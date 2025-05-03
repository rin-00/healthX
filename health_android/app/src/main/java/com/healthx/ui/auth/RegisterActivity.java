package com.healthx.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.healthx.R;
import com.healthx.repository.Resource;
import com.healthx.viewmodel.AuthViewModel;
import com.healthx.viewmodel.ViewModelFactory;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private AuthViewModel authViewModel;
    private TextInputLayout tilUsername, tilEmail, tilPassword, tilNickname;
    private TextInputEditText etUsername, etEmail, etPassword, etNickname;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化视图
        initViews();
        
        // 初始化ViewModel
        authViewModel = new ViewModelProvider(this, new ViewModelFactory(this)).get(AuthViewModel.class);
        
        // 设置监听器
        setupListeners();
        
        // 观察ViewModel的数据变化
        observeViewModel();
    }

    private void initViews() {
        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilNickname = findViewById(R.id.tilNickname);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etNickname = findViewById(R.id.etNickname);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> register());
        
        tvLogin.setOnClickListener(v -> {
            finish(); // 返回登录页面
        });
    }

    private void observeViewModel() {
        // 观察用户名错误
        authViewModel.getUsernameError().observe(this, error -> {
            if (error != null) {
                tilUsername.setError(error);
            } else {
                tilUsername.setError(null);
            }
        });
        
        // 观察密码错误
        authViewModel.getPasswordError().observe(this, error -> {
            if (error != null) {
                tilPassword.setError(error);
            } else {
                tilPassword.setError(null);
            }
        });
        
        // 观察邮箱错误
        authViewModel.getEmailError().observe(this, error -> {
            if (error != null) {
                tilEmail.setError(error);
            } else {
                tilEmail.setError(null);
            }
        });
        
        // 观察昵称错误
        authViewModel.getNicknameError().observe(this, error -> {
            if (error != null) {
                tilNickname.setError(error);
            } else {
                tilNickname.setError(null);
            }
        });
    }

    private void register() {
        // 获取输入
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        
        // 记录注册请求
        Log.d(TAG, "Attempting to register with username: " + username + ", email: " + email);
        
        // 显示进度条
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);
        
        // 调用ViewModel的注册方法
        authViewModel.register(username, password, email, nickname).observe(this, result -> {
            // 记录返回结果
            Log.d(TAG, "Register result status: " + result.getStatus());
            
            // 隐藏进度条
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
            
            if (result.getStatus() == Resource.Status.SUCCESS) {
                Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Register success!");
                // 注册成功，返回登录页面
                finish();
            } else if (result.getStatus() == Resource.Status.ERROR) {
                String errorMsg = result.getMessage() != null ? result.getMessage() : getString(R.string.register_failed);
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Register error: " + errorMsg);
            } else if (result.getStatus() == Resource.Status.LOADING) {
                Log.d(TAG, "Register loading...");
            }
        });
    }
} 