# 体重模块同步功能重构计划

## 一、重构背景

为了保持项目代码的一致性和可维护性，需要将体重(Weight)模块的数据同步功能改为与饮食(Diet)模块相同的实现方式。目前的体重模块使用了不同的同步机制，包括使用Resource类封装和ExecutorService的线程模型，而饮食模块采用了AsyncTask和较为简单的同步方案。

## 二、重构目标

1. 删除体重模块原有的数据同步功能
2. 重新实现与饮食模块相同风格的数据同步功能
3. 保持同步状态定义、数据流转逻辑一致
4. 确保功能完整性，包括添加、更新、删除和批量同步

## 三、重构内容

### 1. 修改模型类

**文件**：`com.healthx.model.WeightRecord`

保持现有的同步状态定义不变，同步状态值含义如下：
- 0：未同步（新创建的记录）
- 1：已同步（与服务器一致）
- 2：需要更新（本地已修改）
- 3：需要删除（标记为删除）

### 2. 修改DAO接口

**文件**：`com.healthx.database.dao.WeightRecordDao`

确保以下方法存在：
```java
@Query("SELECT * FROM weight_records WHERE syncStatus != 1")
List<WeightRecord> getUnsyncedRecords();

@Query("SELECT * FROM weight_records WHERE syncStatus = :syncStatus")
List<WeightRecord> getBySyncStatus(int syncStatus);

@Query("UPDATE weight_records SET syncStatus = :status WHERE id = :id")
void updateSyncStatus(long id, int status);

@Query("UPDATE weight_records SET remoteId = :remoteId, syncStatus = 1 WHERE id = :id")
void updateRemoteId(long id, long remoteId);
```

### 3. 重构Repository层

**文件**：`com.healthx.repository.WeightRepository`

1. **删除原有同步代码**：
   - 移除Resource类相关代码
   - 移除SyncAsyncTask内部类
   - 移除使用ExecutorService的同步实现

2. **按照Diet模块风格新增以下方法**：

```java
// 从服务器获取用户的体重记录
public LiveData<List<WeightRecord>> fetchWeightRecordsFromServer(long userId) {
    MutableLiveData<List<WeightRecord>> recordsData = new MutableLiveData<>();
    
    weightApi.getWeightRecordsByUserId(userId).enqueue(new Callback<ApiResponse<List<WeightRecord>>>() {
        @Override
        public void onResponse(...) {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                List<WeightRecord> records = response.body().getData();
                recordsData.setValue(records);
                // 保存到本地数据库
                for (WeightRecord record : records) {
                    record.setSyncStatus(1); // 已同步
                    new InsertWeightRecordAsyncTask(weightRecordDao).execute(record);
                }
            } else {
                Log.e(TAG, "获取体重记录失败: " + response.message());
            }
        }
        
        @Override
        public void onFailure(...) {
            Log.e(TAG, "网络请求失败: " + t.getMessage());
        }
    });
    
    return recordsData;
}

// 保存体重记录到服务器
public LiveData<WeightRecord> saveWeightRecordToServer(WeightRecord record) {
    MutableLiveData<WeightRecord> recordData = new MutableLiveData<>();
    
    weightApi.addWeightRecord(record).enqueue(new Callback<ApiResponse<WeightRecord>>() {
        @Override
        public void onResponse(...) {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                WeightRecord savedRecord = response.body().getData();
                recordData.setValue(savedRecord);
                
                // 更新本地记录
                record.setRemoteId(savedRecord.getId());
                record.setSyncStatus(1); // 已同步
                new UpdateWeightRecordAsyncTask(weightRecordDao).execute(record);
                
                Log.d(TAG, "体重记录保存成功，ID: " + savedRecord.getId());
            } else {
                Log.e(TAG, "保存体重记录失败: " + response.message());
            }
        }
        
        @Override
        public void onFailure(...) {
            Log.e(TAG, "网络请求失败: " + t.getMessage());
        }
    });
    
    return recordData;
}

// 更新服务器上的体重记录
public LiveData<WeightRecord> updateWeightRecordOnServer(WeightRecord record) {
    MutableLiveData<WeightRecord> recordData = new MutableLiveData<>();
    
    if (record.getRemoteId() == null) {
        // 无远程ID，执行添加操作
        return saveWeightRecordToServer(record);
    }
    
    weightApi.updateWeightRecord(record.getRemoteId(), record).enqueue(new Callback<ApiResponse<WeightRecord>>() {
        @Override
        public void onResponse(...) {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                WeightRecord updatedRecord = response.body().getData();
                recordData.setValue(updatedRecord);
                
                // 更新本地记录
                record.setSyncStatus(1); // 已同步
                new UpdateWeightRecordAsyncTask(weightRecordDao).execute(record);
                
                Log.d(TAG, "体重记录更新成功，ID: " + record.getRemoteId());
            } else {
                Log.e(TAG, "更新体重记录失败: " + response.message());
            }
        }
        
        @Override
        public void onFailure(...) {
            Log.e(TAG, "网络请求失败: " + t.getMessage());
        }
    });
    
    return recordData;
}

// 从服务器删除体重记录
public LiveData<Boolean> deleteWeightRecordOnServer(WeightRecord record) {
    MutableLiveData<Boolean> result = new MutableLiveData<>();
    
    if (record.getRemoteId() == null) {
        // 无远程ID，直接删除本地
        new DeleteWeightRecordAsyncTask(weightRecordDao).execute(record);
        result.setValue(true);
        return result;
    }
    
    weightApi.deleteWeightRecord(record.getRemoteId()).enqueue(new Callback<ApiResponse<Void>>() {
        @Override
        public void onResponse(...) {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                // 从本地删除
                new DeleteWeightRecordAsyncTask(weightRecordDao).execute(record);
                result.setValue(true);
                Log.d(TAG, "体重记录删除成功，ID: " + record.getRemoteId());
            } else {
                Log.e(TAG, "删除体重记录失败: " + response.message());
                result.setValue(false);
            }
        }
        
        @Override
        public void onFailure(...) {
            Log.e(TAG, "网络请求失败: " + t.getMessage());
            result.setValue(false);
        }
    });
    
    return result;
}

// 同步未同步的数据
public void syncUnsyncedData() {
    Log.d(TAG, "开始同步未同步的体重数据...");
    new SyncUnsyncedDataAsyncTask(weightRecordDao, this).execute();
}
```

3. **实现异步任务类**：

```java
// 插入体重记录异步任务
private static class InsertWeightRecordAsyncTask extends AsyncTask<WeightRecord, Void, Void> {
    private WeightRecordDao weightRecordDao;
    
    InsertWeightRecordAsyncTask(WeightRecordDao weightRecordDao) {
        this.weightRecordDao = weightRecordDao;
    }
    
    @Override
    protected Void doInBackground(WeightRecord... records) {
        weightRecordDao.insert(records[0]);
        return null;
    }
}

// 更新体重记录异步任务
private static class UpdateWeightRecordAsyncTask extends AsyncTask<WeightRecord, Void, Void> {
    private WeightRecordDao weightRecordDao;
    
    UpdateWeightRecordAsyncTask(WeightRecordDao weightRecordDao) {
        this.weightRecordDao = weightRecordDao;
    }
    
    @Override
    protected Void doInBackground(WeightRecord... records) {
        weightRecordDao.update(records[0]);
        return null;
    }
}

// 删除体重记录异步任务
private static class DeleteWeightRecordAsyncTask extends AsyncTask<WeightRecord, Void, Void> {
    private WeightRecordDao weightRecordDao;
    
    DeleteWeightRecordAsyncTask(WeightRecordDao weightRecordDao) {
        this.weightRecordDao = weightRecordDao;
    }
    
    @Override
    protected Void doInBackground(WeightRecord... records) {
        weightRecordDao.delete(records[0]);
        return null;
    }
}

// 同步未同步数据异步任务
private static class SyncUnsyncedDataAsyncTask extends AsyncTask<Void, Void, Void> {
    private WeightRecordDao weightRecordDao;
    private WeightRepository repository;
    
    SyncUnsyncedDataAsyncTask(WeightRecordDao weightRecordDao, WeightRepository repository) {
        this.weightRecordDao = weightRecordDao;
        this.repository = repository;
    }
    
    @Override
    protected Void doInBackground(Void... voids) {
        try {
            List<WeightRecord> unsyncedRecords = weightRecordDao.getUnsyncedRecords();
            Log.d("WeightRepository", "找到 " + unsyncedRecords.size() + " 条未同步的体重记录");
            
            for (WeightRecord record : unsyncedRecords) {
                try {
                    Log.d("WeightRepository", "正在同步体重记录，同步状态: " + record.getSyncStatus());
                    
                    switch (record.getSyncStatus()) {
                        case 0: // 未同步
                            repository.saveWeightRecordToServer(record);
                            break;
                        case 2: // 需要更新
                            repository.updateWeightRecordOnServer(record);
                            break;
                        case 3: // 需要删除
                            repository.deleteWeightRecordOnServer(record);
                            break;
                    }
                    
                    // 添加延迟，避免请求过于频繁
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Log.e("WeightRepository", "同步延迟被中断: " + e.getMessage());
                    }
                } catch (Exception e) {
                    Log.e("WeightRepository", "同步单条体重记录时发生异常: " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            Log.e("WeightRepository", "同步体重记录时发生异常: " + e.getMessage(), e);
        }
        return null;
    }
}
```

4. **修改现有的CRUD方法**：

```java
// 插入体重记录
public void insert(WeightRecord record) {
    record.setSyncStatus(0); // 未同步
    new InsertWeightRecordAsyncTask(weightRecordDao).execute(record);
}

// 更新体重记录
public void update(WeightRecord record) {
    record.setSyncStatus(2); // 需要更新
    new UpdateWeightRecordAsyncTask(weightRecordDao).execute(record);
}

// 删除体重记录
public void delete(WeightRecord record) {
    if (record.getRemoteId() != null) {
        record.setSyncStatus(3); // 需要删除
        new UpdateWeightRecordAsyncTask(weightRecordDao).execute(record);
    } else {
        new DeleteWeightRecordAsyncTask(weightRecordDao).execute(record);
    }
}
```

### 4. 修改ViewModel层

**文件**：`com.healthx.viewmodel.WeightViewModel`

1. **修改添加、更新和删除方法**：

```java
// 添加体重记录
public void addWeightRecord(float weight, String note) {
    if (currentUser == null) {
        toastMessage.setValue("请先设置用户信息");
        return;
    }
    
    // 创建新的体重记录
    WeightRecord record = new WeightRecord();
    record.setUserId(currentUser.getId());
    record.setWeight(weight);
    record.setNote(note);
    record.setMeasurementTime(System.currentTimeMillis());
    record.setCreatedAt(System.currentTimeMillis());
    record.setSyncStatus(0); // 未同步
    
    // 如果有身高数据，计算BMI
    if (currentUser.getHeight() > 0) {
        float bmi = WeightRepository.calculateBMI(
                weight, 
                currentUser.getHeight().floatValue());
        record.setBmi(bmi);
        record.setBmiStatus(WeightRepository.getBmiStatus(bmi));
    }
    
    // 保存记录并尝试同步
    repository.insert(record);
    repository.syncUnsyncedData();
    
    // 更新UI状态
    latestWeight.setValue(weight);
    if (record.getBmi() > 0) {
        latestBmi.setValue(record.getBmi());
        bmiStatus.setValue(record.getBmiStatus());
    }
    hasRecordToday.setValue(true);
    toastMessage.setValue("体重记录已保存");
}

// 更新体重记录
public void updateWeightRecord(WeightRecord record, float weight, String note) {
    if (currentUser == null) return;
    
    record.setWeight(weight);
    record.setNote(note);
    
    // 如果有身高数据，重新计算BMI
    if (currentUser.getHeight() > 0) {
        float bmi = WeightRepository.calculateBMI(
                weight, 
                currentUser.getHeight().floatValue());
        record.setBmi(bmi);
        record.setBmiStatus(WeightRepository.getBmiStatus(bmi));
    }
    
    // 保存记录并尝试同步
    repository.update(record);
    repository.syncUnsyncedData();
    
    // 如果是最新记录，更新UI状态
    if (isLatestRecord(record)) {
        latestWeight.setValue(weight);
        if (record.getBmi() > 0) {
            latestBmi.setValue(record.getBmi());
            bmiStatus.setValue(record.getBmiStatus());
        }
    }
    
    toastMessage.setValue("体重记录已更新");
}

// 删除体重记录
public void deleteWeightRecord(WeightRecord record) {
    if (currentUser == null) return;
    
    boolean isToday = DateTimeUtils.isSameDay(new Date(record.getMeasurementTime()), new Date());
    boolean isLatest = isLatestRecord(record);
    
    repository.delete(record);
    repository.syncUnsyncedData();
    
    // 更新UI状态
    if (isToday) {
        checkTodayRecord();
    }
    
    if (isLatest) {
        loadLatestWeightRecord();
    }
    
    toastMessage.setValue("体重记录已删除");
}
```

2. **添加同步方法**：

```java
// 从服务器刷新数据
public void refreshWeightRecordsFromServer() {
    if (currentUser == null) return;
    
    repository.fetchWeightRecordsFromServer(currentUser.getId());
    // 重新加载数据
    loadLatestWeightRecord();
    checkTodayRecord();
}

// 同步未同步的数据
public void syncWeightRecords() {
    if (currentUser == null) return;
    
    repository.syncUnsyncedData();
    toastMessage.setValue("正在同步体重数据...");
}
```

### 5. 修改UI层

**文件**：`com.healthx.ui.weight.WeightFragment`

1. **更新同步按钮点击处理**：

```java
@Override
public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.action_sync) {
        viewModel.syncWeightRecords();
        swipeRefreshLayout.setRefreshing(true);
        // 添加延迟，给用户同步反馈
        new Handler().postDelayed(() -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 1500);
        return true;
    }
    return super.onOptionsItemSelected(item);
}
```

2. **更新下拉刷新处理**：

```java
private void setupSwipeRefresh() {
    if (swipeRefreshLayout != null) {
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark
        );
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 从服务器刷新数据
            viewModel.refreshWeightRecordsFromServer();
            // 添加延迟，给用户刷新反馈
            new Handler().postDelayed(() -> {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 1500);
        });
    }
}
```

## 四、测试计划

1. **单元测试**：
   - 测试体重记录的本地数据库操作
   - 测试同步状态流转逻辑

2. **集成测试**：
   - 测试网络请求和本地数据库交互
   - 测试同步功能是否正常工作

3. **UI测试**：
   - 测试添加、编辑、删除体重记录的UI交互
   - 测试同步按钮和下拉刷新功能

## 五、实施步骤

1. 备份当前体重模块代码
2. 修改WeightRecordDao接口，添加所需查询方法
3. 修改WeightRepository类，删除原有同步代码，添加新的同步实现
4. 修改WeightViewModel类，更新CRUD和同步方法
5. 修改WeightFragment类，更新UI交互处理
6. 进行单元测试和集成测试
7. 进行UI测试
8. 部署新版本并监控运行情况

## 六、风险和注意事项

1. **数据迁移**：确保原有数据在新实现中能够正常访问
2. **线程处理**：从ExecutorService过渡到AsyncTask时注意线程安全问题
3. **兼容性**：确保新实现与现有系统的兼容性
4. **性能考虑**：评估AsyncTask在大量数据处理时的性能表现

## 七、总结

通过此次重构，将体重模块的数据同步功能与饮食模块保持一致，提高了代码的一致性和可维护性。新的实现保留了同步状态管理的核心逻辑，同时采用了与饮食模块相同的AsyncTask方式处理异步操作，并统一了同步功能的实现方式。 