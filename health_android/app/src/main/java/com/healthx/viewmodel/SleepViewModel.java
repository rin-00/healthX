package com.healthx.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.healthx.model.SleepRecord;
import com.healthx.repository.Resource;
import com.healthx.repository.SleepRepository;
import com.healthx.util.DateTimeUtils;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SleepViewModel extends AndroidViewModel {
    
    private final SleepRepository sleepRepository;
    private final MutableLiveData<Long> userId = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>();
    
    // 存储最近7天的睡眠记录
    private final LiveData<List<SleepRecord>> last7DaysSleepRecords;
    
    // 当天的睡眠记录
    private final LiveData<SleepRecord> todaySleepRecord;
    
    // 保存最近添加/更新的睡眠记录
    private SleepRecord currentSleepRecord;
    
    public SleepViewModel(@NonNull Application application) {
        super(application);
        sleepRepository = SleepRepository.getInstance(application);
        
        // 初始化选定日期为今天
        selectedDate.setValue(LocalDate.now());
        
        // 根据用户ID和选定日期获取睡眠记录
        todaySleepRecord = Transformations.switchMap(selectedDate, date -> 
                Transformations.switchMap(userId, id -> 
                        sleepRepository.getUserSleepRecordForDate(id, date)));
        
        // 获取最近7天的睡眠记录
        last7DaysSleepRecords = Transformations.switchMap(userId, id -> 
                sleepRepository.getUserLast7DaysSleepRecords(id));
    }
    
    // 设置用户ID
    public void setUserId(long id) {
        if (userId.getValue() == null || userId.getValue() != id) {
            userId.setValue(id);
        }
    }
    
    // 设置选定日期
    public void setSelectedDate(LocalDate date) {
        if (!date.equals(selectedDate.getValue())) {
            selectedDate.setValue(date);
        }
    }
    
    // 获取今天的睡眠记录
    public LiveData<SleepRecord> getTodaySleepRecord() {
        return todaySleepRecord;
    }
    
    // 获取最近7天的睡眠记录
    public LiveData<List<SleepRecord>> getLast7DaysSleepRecords() {
        return last7DaysSleepRecords;
    }
    
    // 获取指定用户的最近7天睡眠记录
    public LiveData<List<SleepRecord>> getLast7DaysSleepRecords(long userId) {
        return sleepRepository.getUserLast7DaysSleepRecords(userId);
    }
    
    // 添加睡眠记录
    public LiveData<Resource<SleepRecord>> addSleepRecord(LocalDateTime startTime, LocalDateTime endTime) {
        if (userId.getValue() == null) {
            MutableLiveData<Resource<SleepRecord>> result = new MutableLiveData<>();
            result.setValue(Resource.error("用户ID未设置", null));
            return result;
        }
        
        SleepRecord sleepRecord = new SleepRecord(userId.getValue(), startTime, endTime);
        currentSleepRecord = sleepRecord;
        return sleepRepository.addSleepRecord(sleepRecord);
    }
    
    // 使用指定的userId添加睡眠记录
    public LiveData<Resource<SleepRecord>> addSleepRecord(long userId, LocalDateTime startTime, LocalDateTime endTime) {
        SleepRecord sleepRecord = new SleepRecord(userId, startTime, endTime);
        currentSleepRecord = sleepRecord;
        return sleepRepository.addSleepRecord(sleepRecord);
    }
    
    // 更新睡眠记录
    public LiveData<Resource<SleepRecord>> updateSleepRecord(SleepRecord sleepRecord) {
        currentSleepRecord = sleepRecord;
        return sleepRepository.updateSleepRecord(sleepRecord);
    }
    
    // 删除睡眠记录
    public LiveData<Resource<Boolean>> deleteSleepRecord(SleepRecord sleepRecord) {
        return sleepRepository.deleteSleepRecord(sleepRecord);
    }
    
    // 同步数据
    public LiveData<Resource<Boolean>> syncData() {
        if (userId.getValue() == null) {
            MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
            result.setValue(Resource.error("用户ID未设置", false));
            return result;
        }
        
        return sleepRepository.syncData(userId.getValue());
    }
    
    // 强制重新加载数据
    public void reloadData() {
        if (userId.getValue() != null) {
            // 通过临时修改用户ID，然后恢复的方式触发数据重新加载
            long currentId = userId.getValue();
            userId.setValue(null);  // 清空
            userId.setValue(currentId);  // 恢复，触发数据加载
        }
    }
    
    // 清除本地重复数据
    public LiveData<Resource<Boolean>> cleanupDuplicateRecords() {
        if (userId.getValue() == null) {
            MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
            result.setValue(Resource.error("用户ID未设置", false));
            return result;
        }
        
        return sleepRepository.cleanupDuplicateRecords(userId.getValue());
    }
    
    // 获取睡眠时长（小时和分钟格式）
    public String formatDuration(int durationMinutes) {
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        return String.format("%d小时%d分钟", hours, minutes);
    }
    
    // 获取睡眠时长（格式化为文本）
    public String getSleepDurationText(SleepRecord sleepRecord) {
        if (sleepRecord == null) {
            return "未记录";
        }
        
        return formatDuration(sleepRecord.getDuration());
    }
    
    // 获取睡眠时间段文本
    public String getSleepTimeRangeText(SleepRecord sleepRecord) {
        if (sleepRecord == null) {
            return "未记录";
        }
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String startTime = sleepRecord.getStartTime().format(timeFormatter);
        String endTime = sleepRecord.getEndTime().format(timeFormatter);
        
        return startTime + " - " + endTime;
    }
    
    // 检查今天是否已记录睡眠时间
    public boolean isTodaySleepRecorded() {
        return todaySleepRecord.getValue() != null;
    }
    
    // 获取当前正在编辑的睡眠记录
    public SleepRecord getCurrentSleepRecord() {
        return currentSleepRecord;
    }
    
    // 计算过去7天的平均睡眠时长（分钟）
    public int calculateAverageSleepDuration() {
        List<SleepRecord> records = last7DaysSleepRecords.getValue();
        if (records == null || records.isEmpty()) {
            return 0;
        }
        
        int totalMinutes = 0;
        for (SleepRecord record : records) {
            totalMinutes += record.getDuration();
        }
        
        return totalMinutes / records.size();
    }
    
    // 获取最近7天平均睡眠时长文本
    public String getAverageSleepDurationText() {
        int avgMinutes = calculateAverageSleepDuration();
        if (avgMinutes == 0) {
            return "未记录";
        }
        
        return formatDuration(avgMinutes);
    }
    
    // 提取日期部分（用于按日期分组）
    public LocalDate extractDate(LocalDateTime dateTime) {
        return dateTime.toLocalDate();
    }
    
    // 获取过去7天的日期列表（用于显示图表）
    public List<LocalDate> getLast7Days() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            dates.add(today.minusDays(i));
        }
        
        return dates;
    }
    
    // 获取指定日期的睡眠时长（分钟），如果没有记录则返回0
    public int getSleepDurationForDate(LocalDate date) {
        if (last7DaysSleepRecords.getValue() == null) {
            return 0;
        }
        
        for (SleepRecord record : last7DaysSleepRecords.getValue()) {
            if (extractDate(record.getStartTime()).equals(date)) {
                return record.getDuration();
            }
        }
        
        return 0;
    }
    
    // 获取过去7天的睡眠时长数据（用于图表显示）
    public List<Integer> getLast7DaysSleepDurations() {
        List<Integer> durations = new ArrayList<>();
        List<LocalDate> dates = getLast7Days();
        
        for (LocalDate date : dates) {
            durations.add(getSleepDurationForDate(date));
        }
        
        return durations;
    }
    
    // 判断睡眠质量（基于睡眠时长）
    public String evaluateSleepQuality(int durationMinutes) {
        if (durationMinutes < 360) { // 少于6小时
            return "不足";
        } else if (durationMinutes >= 360 && durationMinutes <= 480) { // 6-8小时
            return "良好";
        } else if (durationMinutes > 480 && durationMinutes <= 540) { // 8-9小时
            return "优秀";
        } else { // 超过9小时
            return "过量";
        }
    }
    
    // 获取睡眠记录的睡眠质量
    public String getSleepQuality(SleepRecord sleepRecord) {
        if (sleepRecord == null) {
            return "未记录";
        }
        
        return evaluateSleepQuality(sleepRecord.getDuration());
    }
    
    // 检查指定日期是否已经存在睡眠记录
    public LiveData<Boolean> hasSleepRecordForDate(LocalDate date) {
        if (userId.getValue() == null) {
            MutableLiveData<Boolean> result = new MutableLiveData<>();
            result.setValue(false);
            return result;
        }
        
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        // 先获取该日期的记录
        sleepRepository.getUserSleepRecordForDate(userId.getValue(), date).observeForever(record -> {
            result.setValue(record != null);
        });
        
        return result;
    }
    
    // 检查指定日期是否已经存在睡眠记录（同步方式，用于界面显示前检查）
    public boolean checkDateHasRecord(LocalDate date, SleepRecord existingRecord) {
        if (userId.getValue() == null) {
            return false;
        }
        
        // 从缓存中获取最近7天的记录
        List<SleepRecord> records = last7DaysSleepRecords.getValue();
        if (records != null) {
            for (SleepRecord record : records) {
                if (record.getStartTime() != null && 
                    record.getStartTime().toLocalDate().equals(date)) {
                    // 如果是编辑现有记录，且该记录就是当前日期的记录，则不算重复
                    if (existingRecord != null && record.getId() == existingRecord.getId()) {
                        continue;
                    }
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // 以LiveData形式检查指定日期是否有睡眠记录（可用于UI中显示）
    public LiveData<Boolean> checkDateHasRecordAsync(LocalDate date, SleepRecord existingRecord) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        if (userId.getValue() == null) {
            result.setValue(false);
            return result;
        }
        
        // 先尝试从缓存中检查
        List<SleepRecord> records = last7DaysSleepRecords.getValue();
        if (records != null) {
            boolean hasRecord = false;
            for (SleepRecord record : records) {
                if (record.getStartTime() != null && 
                    record.getStartTime().toLocalDate().equals(date)) {
                    // 如果是编辑现有记录，且该记录就是当前日期的记录，则不算重复
                    if (existingRecord != null && record.getId() == existingRecord.getId()) {
                        continue;
                    }
                    hasRecord = true;
                    break;
                }
            }
            
            result.setValue(hasRecord);
            return result;
        }
        
        // 如果缓存中没有相关记录，则查询今天的记录
        sleepRepository.getUserSleepRecordForDate(userId.getValue(), date)
            .observeForever(sleepRecord -> {
                boolean hasRecord = sleepRecord != null;
                if (hasRecord && existingRecord != null && existingRecord.getId() == sleepRecord.getId()) {
                    // 如果是编辑现有记录，则不算重复
                    hasRecord = false;
                }
                result.setValue(hasRecord);
            });
        
        return result;
    }
} 