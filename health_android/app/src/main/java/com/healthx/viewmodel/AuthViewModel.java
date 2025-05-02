package com.healthx.viewmodel;

import android.text.TextUtils;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.healthx.model.User;
import com.healthx.repository.Resource;
import com.healthx.repository.UserRepository;
import com.healthx.util.Constants;

import java.util.regex.Pattern;

/**
 * 认证ViewModel，处理登录和注册逻辑
 */
public class AuthViewModel extends ViewModel {
    
    private final UserRepository userRepository;
    
    private final MutableLiveData<String> usernameError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> nicknameError = new MutableLiveData<>();
    
    public AuthViewModel() {
        userRepository = new UserRepository();
    }
    
    /**
     * 登录
     */
    public LiveData<Resource<User>> login(String username, String password) {
        // 验证输入
        if (!validateLoginInput(username, password)) {
            return new MutableLiveData<>(Resource.error("请修正输入错误", null));
        }
        
        return userRepository.login(username, password);
    }
    
    /**
     * 注册
     */
    public LiveData<Resource<User>> register(String username, String password, String email, String nickname) {
        // 验证输入
        if (!validateRegisterInput(username, password, email, nickname)) {
            return new MutableLiveData<>(Resource.error("请修正输入错误", null));
        }
        
        return userRepository.register(username, password, email, nickname);
    }
    
    /**
     * 检查用户名是否存在
     */
    public LiveData<Resource<Boolean>> checkUsername(String username) {
        return userRepository.checkUsername(username);
    }
    
    /**
     * 检查邮箱是否存在
     */
    public LiveData<Resource<Boolean>> checkEmail(String email) {
        return userRepository.checkEmail(email);
    }
    
    /**
     * 验证登录输入
     */
    private boolean validateLoginInput(String username, String password) {
        boolean isValid = true;
        
        if (TextUtils.isEmpty(username)) {
            usernameError.setValue("用户名不能为空");
            isValid = false;
        } else {
            usernameError.setValue(null);
        }
        
        if (TextUtils.isEmpty(password)) {
            passwordError.setValue("密码不能为空");
            isValid = false;
        } else if (password.length() < 6) {
            passwordError.setValue("密码长度不能少于6位");
            isValid = false;
        } else {
            passwordError.setValue(null);
        }
        
        return isValid;
    }
    
    /**
     * 验证注册输入
     */
    private boolean validateRegisterInput(String username, String password, String email, String nickname) {
        boolean isValid = true;
        
        // 验证用户名
        if (TextUtils.isEmpty(username)) {
            usernameError.setValue("用户名不能为空");
            isValid = false;
        } else if (username.length() < 3 || username.length() > 20) {
            usernameError.setValue("用户名长度必须在3-20之间");
            isValid = false;
        } else if (!Pattern.matches(Constants.USERNAME_PATTERN, username)) {
            usernameError.setValue("用户名只能包含字母、数字、下划线和短横线");
            isValid = false;
        } else {
            usernameError.setValue(null);
        }
        
        // 验证密码
        if (TextUtils.isEmpty(password)) {
            passwordError.setValue("密码不能为空");
            isValid = false;
        } else if (password.length() < 6 || password.length() > 20) {
            passwordError.setValue("密码长度必须在6-20之间");
            isValid = false;
        } else if (!Pattern.matches(Constants.PASSWORD_PATTERN, password)) {
            passwordError.setValue("密码只能包含字母、数字、下划线和短横线");
            isValid = false;
        } else {
            passwordError.setValue(null);
        }
        
        // 验证邮箱
        if (TextUtils.isEmpty(email)) {
            emailError.setValue("邮箱不能为空");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.setValue("邮箱格式不正确");
            isValid = false;
        } else {
            emailError.setValue(null);
        }
        
        // 验证昵称
        if (TextUtils.isEmpty(nickname)) {
            nicknameError.setValue("昵称不能为空");
            isValid = false;
        } else {
            nicknameError.setValue(null);
        }
        
        return isValid;
    }
    
    // Getters for error LiveData
    public LiveData<String> getUsernameError() {
        return usernameError;
    }
    
    public LiveData<String> getPasswordError() {
        return passwordError;
    }
    
    public LiveData<String> getEmailError() {
        return emailError;
    }
    
    public LiveData<String> getNicknameError() {
        return nicknameError;
    }
    
    /**
     * 退出登录
     */
    public void logout() {
        userRepository.logout();
    }
} 