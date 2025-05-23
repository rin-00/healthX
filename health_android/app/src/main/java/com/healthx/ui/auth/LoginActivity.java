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
import com.healthx.MainActivity;
import com.healthx.R;
import com.healthx.model.User;
import com.healthx.network.TokenManager;
import com.healthx.repository.Resource;
import com.healthx.viewmodel.AuthViewModel;
import com.healthx.viewmodel.ViewModelFactory;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private AuthViewModel authViewModel;
    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化TokenManager - 现在在Application中已经初始化，这里只是安全检查
        if (TokenManager.getInstance().getSharedPreferences() == null) {
            TokenManager.getInstance().init(this);
        }
        
        // 检查用户是否已登录，如果已登录，直接进入主页
        if (TokenManager.getInstance().isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

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
        try {
            tilUsername = findViewById(R.id.tilUsername);
            tilPassword = findViewById(R.id.tilPassword);
            etUsername = findViewById(R.id.etUsername);
            etPassword = findViewById(R.id.etPassword);
            btnLogin = findViewById(R.id.btnLogin);
            tvRegister = findViewById(R.id.tvRegister);
            progressBar = findViewById(R.id.progressBar);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());
        
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
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
    }

    private void login() {
        try {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            
            // 显示进度条
            progressBar.setVisibility(View.VISIBLE);
            
            // 调用ViewModel的登录方法
            authViewModel.login(username, password).observe(this, result -> {
                // 隐藏进度条
                progressBar.setVisibility(View.GONE);
                
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    User user = result.getData();
                    Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                    // 延迟200毫秒再跳转，确保Toast显示
                    progressBar.postDelayed(this::navigateToMainActivity, 200);
                } else if (result.getStatus() == Resource.Status.ERROR) {
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error during login: " + e.getMessage());
            Toast.makeText(this, "登录过程中发生错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navigateToMainActivity() {
        try {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to MainActivity: " + e.getMessage());
            Toast.makeText(this, "无法进入主页: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 