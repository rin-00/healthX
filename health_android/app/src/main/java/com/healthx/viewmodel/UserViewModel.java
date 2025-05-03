package com.healthx.viewmodel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.healthx.model.User;
import com.healthx.network.ApiClient;
import com.healthx.network.model.ApiResponse;
import com.healthx.network.model.UserResponse;
import com.healthx.network.UserApiService;
import com.healthx.repository.UserRepository;
import com.healthx.util.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserViewModel extends AndroidViewModel {

    private static final String TAG = "UserViewModel";
    private final UserRepository userRepository;
    private UserApiService userApiService;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        
        // 确保使用正确的API地址初始化
        ApiClient apiClient = ApiClient.getInstance();
        // 如果需要，可以重置为Constants.API_BASE_URL确保一致性
        // apiClient = ApiClient.resetInstance(Constants.API_BASE_URL);
        userApiService = apiClient.create(UserApiService.class);
        
        Log.d(TAG, "UserViewModel初始化完成，使用API地址: " + Constants.API_BASE_URL);
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     */
    public void getUserById(long userId) {
        Log.d(TAG, "尝试获取用户数据, userId: " + userId);
        loadingLiveData.setValue(true);

        // 先尝试从本地缓存获取
        User cachedUser = userRepository.getUserById(userId);
        if (cachedUser != null) {
            Log.d(TAG, "使用缓存数据: " + cachedUser.getUsername());
            userLiveData.setValue(cachedUser);
        }

        // 再从网络获取最新数据
        userApiService.getUserById(userId).enqueue(createUserCallback(userId));
    }

    /**
     * 更新用户信息
     *
     * @param user 用户对象
     */
    public void updateUser(User user) {
        if (user == null || user.getId() == null) {
            errorLiveData.setValue("用户数据无效");
            return;
        }

        Log.d(TAG, "尝试更新用户数据, userId: " + user.getId());
        loadingLiveData.setValue(true);

        userApiService.updateUser(user.getId(), user).enqueue(createUpdateCallback(user, "更新用户信息失败", 
            (failedUser) -> tryAlternateServerAddressesForUpdateUser(failedUser)));
    }

    /**
     * 更新用户基本资料（昵称和邮箱）
     *
     * @param user 用户对象，只需包含ID、昵称和邮箱
     */
    public void updateUserProfile(User user) {
        if (user == null || user.getId() == null) {
            errorLiveData.setValue("用户数据无效");
            return;
        }

        Log.d(TAG, "尝试更新用户基本资料, userId: " + user.getId());
        loadingLiveData.setValue(true);

        userApiService.updateUserProfile(user.getId(), user).enqueue(createUpdateCallback(user, "更新用户基本资料失败", 
            (failedUser) -> tryAlternateServerAddressesForProfileUpdate(failedUser)));
    }

    /**
     * 更新用户健康数据（性别、年龄、身高、体重）
     *
     * @param user 用户对象，只需包含ID和健康数据
     */
    public void updateHealthData(User user) {
        if (user == null || user.getId() == null) {
            errorLiveData.setValue("用户数据无效");
            return;
        }

        Log.d(TAG, "尝试更新用户健康数据, userId: " + user.getId());
        loadingLiveData.setValue(true);

        userApiService.updateUserHealthData(user.getId(), user).enqueue(createUpdateCallback(user, "更新用户健康数据失败", 
            (failedUser) -> tryAlternateServerAddressesForHealthUpdate(failedUser)));
    }

    /**
     * 创建获取用户数据的回调
     */
    private Callback<ApiResponse<UserResponse>> createUserCallback(long userId) {
        return new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Response<ApiResponse<UserResponse>> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse userResponse = response.body().getData();
                    if (userResponse != null) {
                        Log.d(TAG, "从网络获取用户数据成功: " + userResponse.getUsername());
                        
                        // 将UserResponse转换为User
                        User user = convertToUser(userResponse);
                        
                        // 更新LiveData
                        userLiveData.setValue(user);
                        // 更新本地缓存
                        userRepository.saveUser(user);
                    } else {
                        Log.e(TAG, "API返回了空的用户数据");
                        errorLiveData.setValue("服务器返回了空的用户数据");
                    }
                } else {
                    String errorMsg = "获取用户信息失败";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    } else if (!response.isSuccessful()) {
                        errorMsg = "HTTP错误: " + response.code();
                    }
                    Log.e(TAG, "API错误: " + errorMsg);
                    errorLiveData.setValue(errorMsg);
                    
                    // 如果是服务器连接错误，尝试替代地址
                    if (response.code() >= 500 || response.code() == 404) {
                        tryAlternateServerAddresses(userId);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Throwable t) {
                loadingLiveData.setValue(false);
                String errorMsg = "网络请求失败: " + t.getMessage();
                Log.e(TAG, "网络错误: " + t.getMessage(), t);
                errorLiveData.setValue(errorMsg);
                
                // 尝试使用其他可用的服务器地址
                tryAlternateServerAddresses(userId);
            }
        };
    }

    /**
     * 创建通用的更新回调
     */
    private Callback<ApiResponse<UserResponse>> createUpdateCallback(User user, String errorPrefix, UpdateRetryCallback retryCallback) {
        return new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Response<ApiResponse<UserResponse>> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse userResponse = response.body().getData();
                    if (userResponse != null) {
                        Log.d(TAG, "更新用户数据成功: " + userResponse.getUsername());
                        
                        // 将UserResponse转换为User
                        User updatedUser = convertToUser(userResponse);
                        
                        // 更新LiveData
                        userLiveData.setValue(updatedUser);
                        // 更新本地缓存
                        userRepository.saveUser(updatedUser);
                    } else {
                        Log.e(TAG, "API返回了空的用户数据");
                        errorLiveData.setValue("服务器返回了空的用户数据");
                    }
                } else {
                    String errorMsg = errorPrefix;
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    } else if (!response.isSuccessful()) {
                        errorMsg = "HTTP错误: " + response.code();
                    }
                    Log.e(TAG, "API错误: " + errorMsg);
                    errorLiveData.setValue(errorMsg);
                    
                    // 如果是服务器连接错误，尝试替代地址
                    if (response.code() >= 500 || response.code() == 404) {
                        retryCallback.retry(user);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Throwable t) {
                loadingLiveData.setValue(false);
                String errorMsg = "网络请求失败: " + t.getMessage();
                Log.e(TAG, "网络错误: " + t.getMessage(), t);
                errorLiveData.setValue(errorMsg);
                
                // 尝试使用其他可用的服务器地址
                retryCallback.retry(user);
            }
        };
    }

    /**
     * 更新重试回调接口
     */
    private interface UpdateRetryCallback {
        void retry(User user);
    }

    /**
     * 尝试使用替代服务器地址获取用户数据
     */
    private void tryAlternateServerAddresses(long userId) {
        Log.d(TAG, "尝试使用替代服务器地址获取用户数据");
        
        // 可能的替代服务器地址
        String[] alternateAddresses = {
            "http://10.0.2.2:8080/",    // 标准模拟器地址
            "http://127.0.0.1:8080/",   // 本地回环地址
            "http://localhost:8080/",   // localhost
            Constants.API_BASE_URL,     // 主要配置地址
            "http://192.168.0.101:8080/" // 本机局域网IP（需要替换为实际IP）
        };
        
        // 尝试连接到不同的服务器地址
        for (String address : alternateAddresses) {
            Log.d(TAG, "尝试连接到地址: " + address);
            
            // 重置ApiClient并使用新地址
            ApiClient apiClient = ApiClient.resetInstance(address);
            userApiService = apiClient.create(UserApiService.class);
            
            // 使用新的服务重新尝试请求
            try {
                // 重新尝试获取用户数据
                Log.d(TAG, "使用新的API地址重试获取用户数据");
                userApiService.getUserById(userId).enqueue(createUserCallback(userId));
                return; // 如果没有抛出异常，则成功启动了请求
            } catch (Exception e) {
                Log.e(TAG, "尝试连接到 " + address + " 失败: " + e.getMessage());
            }
        }
        
        Log.e(TAG, "所有替代服务器地址均连接失败");
    }
    
    /**
     * 尝试使用替代服务器地址更新用户数据
     */
    private void tryAlternateServerAddressesForUpdateUser(User user) {
        Log.d(TAG, "尝试使用替代服务器地址更新用户数据");
        
        // 可能的替代服务器地址
        String[] alternateAddresses = {
            "http://10.0.2.2:8080/",    // 标准模拟器地址
            "http://127.0.0.1:8080/",   // 本地回环地址
            "http://localhost:8080/",   // localhost
            Constants.API_BASE_URL,     // 主要配置地址
            "http://192.168.0.101:8080/" // 本机局域网IP（需要替换为实际IP）
        };
        
        // 尝试连接到不同的服务器地址
        for (String address : alternateAddresses) {
            Log.d(TAG, "尝试连接到地址: " + address);
            
            // 重置ApiClient并使用新地址
            ApiClient apiClient = ApiClient.resetInstance(address);
            userApiService = apiClient.create(UserApiService.class);
            
            // 使用新的服务重新尝试请求
            try {
                // 重新尝试更新用户数据
                Log.d(TAG, "使用新的API地址重试更新用户数据");
                updateUser(user);
                return; // 如果没有抛出异常，则成功启动了请求
            } catch (Exception e) {
                Log.e(TAG, "尝试连接到 " + address + " 失败: " + e.getMessage());
            }
        }
        
        Log.e(TAG, "所有替代服务器地址均连接失败");
    }
    
    /**
     * 尝试使用替代服务器地址更新用户个人资料
     */
    private void tryAlternateServerAddressesForProfileUpdate(User user) {
        Log.d(TAG, "尝试使用替代服务器地址更新用户个人资料");
        
        // 可能的替代服务器地址
        String[] alternateAddresses = {
            "http://10.0.2.2:8080/",    // 标准模拟器地址
            "http://127.0.0.1:8080/",   // 本地回环地址
            "http://localhost:8080/",   // localhost
            Constants.API_BASE_URL,     // 主要配置地址
            "http://192.168.0.101:8080/" // 本机局域网IP（需要替换为实际IP）
        };
        
        // 尝试连接到不同的服务器地址
        for (String address : alternateAddresses) {
            Log.d(TAG, "尝试连接到地址: " + address);
            
            // 重置ApiClient并使用新地址
            ApiClient apiClient = ApiClient.resetInstance(address);
            userApiService = apiClient.create(UserApiService.class);
            
            // 使用新的服务重新尝试请求
            try {
                // 重新尝试更新用户个人资料
                Log.d(TAG, "使用新的API地址重试更新用户个人资料");
                updateUserProfile(user);
                return; // 如果没有抛出异常，则成功启动了请求
            } catch (Exception e) {
                Log.e(TAG, "尝试连接到 " + address + " 失败: " + e.getMessage());
            }
        }
        
        Log.e(TAG, "所有替代服务器地址均连接失败");
    }
    
    /**
     * 尝试使用替代服务器地址更新用户健康数据
     */
    private void tryAlternateServerAddressesForHealthUpdate(User user) {
        Log.d(TAG, "尝试使用替代服务器地址更新用户健康数据");
        
        // 可能的替代服务器地址
        String[] alternateAddresses = {
            "http://10.0.2.2:8080/",    // 标准模拟器地址
            "http://127.0.0.1:8080/",   // 本地回环地址
            "http://localhost:8080/",   // localhost
            Constants.API_BASE_URL,     // 主要配置地址
            "http://192.168.0.101:8080/" // 本机局域网IP（需要替换为实际IP）
        };
        
        // 尝试连接到不同的服务器地址
        for (String address : alternateAddresses) {
            Log.d(TAG, "尝试连接到地址: " + address);
            
            // 重置ApiClient并使用新地址
            ApiClient apiClient = ApiClient.resetInstance(address);
            userApiService = apiClient.create(UserApiService.class);
            
            // 使用新的服务重新尝试请求
            try {
                // 重新尝试更新用户健康数据
                Log.d(TAG, "使用新的API地址重试更新用户健康数据");
                updateHealthData(user);
                return; // 如果没有抛出异常，则成功启动了请求
            } catch (Exception e) {
                Log.e(TAG, "尝试连接到 " + address + " 失败: " + e.getMessage());
            }
        }
        
        Log.e(TAG, "所有替代服务器地址均连接失败");
    }

    /**
     * 获取缓存的用户数据
     *
     * @return 缓存的用户对象，如果没有缓存则返回null
     */
    public User getCachedUser() {
        // 尝试获取上次加载的用户
        User cachedUser = userLiveData.getValue();
        if (cachedUser != null) {
            Log.d(TAG, "从ViewModel缓存中获取用户数据: " + cachedUser.getUsername());
            return cachedUser;
        }
        
        // 从本地数据库获取用户
        long userId = getApplication().getSharedPreferences("health_prefs", Context.MODE_PRIVATE)
                .getLong("user_id", -1);
        if (userId != -1) {
            User localUser = userRepository.getUserById(userId);
            if (localUser != null) {
                Log.d(TAG, "从本地数据库获取用户数据: " + localUser.getUsername());
                return localUser;
            }
        }
        
        Log.d(TAG, "无可用的缓存用户数据");
        return null;
    }

    /**
     * 将UserResponse转换为User对象
     */
    private User convertToUser(UserResponse response) {
        User user = new User(
            response.getId(),
            response.getUsername(),
            response.getEmail(),
            response.getNickname(),
            null  // token在这里不需要设置
        );
        
        user.setGender(response.getGender());
        user.setAge(response.getAge());
        user.setHeight(response.getHeight());
        user.setWeight(response.getWeight());
        
        return user;
    }
} 