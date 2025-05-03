package com.healthx.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.healthx.R;
import com.healthx.model.User;
import com.healthx.viewmodel.UserViewModel;
import com.healthx.viewmodel.ViewModelFactory;

import java.text.DecimalFormat;

import de.hdodenhof.circleimageview.CircleImageView;
import com.google.android.material.snackbar.Snackbar;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    
    private TextView usernameText;
    private TextView emailText;
    private CircleImageView avatarImage;
    private Button editProfileButton;
    private TextView settingsText;
    private TextView aboutText;
    private TextView logoutText;
    private TextView healthRecordsText;
    
    // 健康数据相关的视图
    private TextView genderText;
    private TextView ageText;
    private TextView heightText;
    private TextView weightText;
    private TextView bmiText;
    private TextView bmiStatusText;
    private LinearProgressIndicator bmiProgressIndicator;
    private Button updateHealthDataButton;
    
    private UserViewModel userViewModel;
    private SharedPreferences sharedPreferences;
    private User currentUser;
    
    private static final DecimalFormat df = new DecimalFormat("0.0");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ProfileFragment创建");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: 创建ProfileFragment视图");
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Log.d(TAG, "onCreateView: 视图创建结果: " + (view != null ? "成功" : "失败"));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: 初始化Fragment视图");
        initViews(view);
        initViewModel();
        setupUserData();
        setupListeners();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ProfileFragment已恢复");
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ProfileFragment已暂停");
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ProfileFragment视图销毁");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ProfileFragment销毁");
    }
    
    private void initViewModel() {
        Log.d(TAG, "initViewModel: 初始化ViewModel");
        userViewModel = new ViewModelProvider(requireActivity(), 
            new ViewModelFactory(requireActivity())).get(UserViewModel.class);
        sharedPreferences = requireActivity().getSharedPreferences("health_prefs", Context.MODE_PRIVATE);
        
        // 观察用户数据变化
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            Log.d(TAG, "用户数据变更: " + (user != null ? "有数据" : "无数据"));
            if (user != null) {
                currentUser = user;
                updateUI(user);
            } else {
                // 用户数据为空时，尝试从缓存加载
                Log.d(TAG, "尝试从缓存加载用户数据 (用户数据为空)");
                User cachedUser = loadUserFromCache();
                if (cachedUser != null) {
                    currentUser = cachedUser;
                    updateUI(cachedUser);
                } else {
                    // 如果缓存也没有数据，才加载测试数据
                    Log.d(TAG, "加载测试数据 (缓存数据为空)");
                    updateDummyData();
                }
            }
        });
        
        // 加载用户数据
        long userId = sharedPreferences.getLong("user_id", -1);
        Log.d(TAG, "从SharedPreferences获取用户ID: " + userId);
        if (userId != -1) {
            Log.d(TAG, "尝试从网络加载用户数据");
            loadingState(true); // 显示加载状态
            userViewModel.getUserById(userId);
        } else {
            // 用户ID不存在时，尝试从缓存加载
            Log.d(TAG, "尝试从缓存加载用户数据 (用户ID不存在)");
            User cachedUser = loadUserFromCache();
            if (cachedUser != null) {
                currentUser = cachedUser;
                updateUI(cachedUser);
            } else {
                // 如果缓存也没有数据，才加载测试数据
                Log.d(TAG, "加载测试数据 (无用户ID且缓存为空)");
                updateDummyData();
            }
        }
        
        // 监听加载状态
        userViewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            loadingState(isLoading != null && isLoading);
        });
        
        // 监听错误消息
        userViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "加载用户数据错误: " + error);
                
                // 尝试从缓存加载数据
                User cachedUser = loadUserFromCache();
                if (cachedUser != null) {
                    // 有缓存数据时使用缓存数据
                    currentUser = cachedUser;
                    updateUI(cachedUser);
                    
                    // 显示一个提示，告知用户正在使用缓存数据
                    Toast.makeText(requireContext(), 
                        "网络连接失败，显示本地缓存数据。请检查网络连接。", 
                        Toast.LENGTH_LONG).show();
                } else {
                    // 缓存也没有数据时，显示错误提示并使用测试数据
                    Toast.makeText(requireContext(), 
                        "加载用户数据失败: " + error + "\n显示测试数据。", 
                        Toast.LENGTH_LONG).show();
                    updateDummyData();
                    
                    // 添加重试按钮
                    showRetrySnackbar();
                }
            }
        });
    }

    private void initViews(View view) {
        Log.d(TAG, "initViews: 初始化视图");
        try {
            // 基本信息
            usernameText = view.findViewById(R.id.text_username);
            emailText = view.findViewById(R.id.text_email);
            avatarImage = view.findViewById(R.id.image_avatar);
            editProfileButton = view.findViewById(R.id.button_edit_profile);
            
            // 健康数据
            genderText = view.findViewById(R.id.text_gender);
            ageText = view.findViewById(R.id.text_age);
            heightText = view.findViewById(R.id.text_height);
            weightText = view.findViewById(R.id.text_weight);
            bmiText = view.findViewById(R.id.text_bmi);
            bmiStatusText = view.findViewById(R.id.text_bmi_status);
            bmiProgressIndicator = view.findViewById(R.id.progress_bmi);
            updateHealthDataButton = view.findViewById(R.id.button_update_health_data);
            
            // 设置项
            settingsText = view.findViewById(R.id.text_settings);
            healthRecordsText = view.findViewById(R.id.text_health_records);
            aboutText = view.findViewById(R.id.text_about);
            logoutText = view.findViewById(R.id.text_logout);
            
            Log.d(TAG, "initViews: 初始化视图成功");
        } catch (Exception e) {
            Log.e(TAG, "initViews: 初始化视图失败", e);
        }
    }

    private void setupUserData() {
        Log.d(TAG, "setupUserData: 设置用户数据");
        // 测试数据，以后会被真实数据替换
        updateDummyData();
    }
    
    private void updateDummyData() {
        Log.d(TAG, "更新测试数据");
        // 测试用户数据，实际应从ViewModel获取
        usernameText.setText("测试昵称");
        emailText.setText("test@example.com");
        
        genderText.setText("男");
        ageText.setText("28岁");
        heightText.setText("175 cm");
        weightText.setText("70 kg");
        
        // 计算BMI
        double height = 1.75; // 米
        double weight = 70.0; // 千克
        double bmi = calculateBMI(height, weight);
        updateBMIDisplay(bmi);
    }
    
    private void updateUI(User user) {
        Log.d(TAG, "更新UI: 用户名 = " + user.getUsername() + ", 昵称 = " + user.getNickname());
        if (user == null) return;
        
        // 使用昵称而不是用户名
        if (user.getNickname() != null && !user.getNickname().isEmpty()) {
            usernameText.setText(user.getNickname());
        } else {
            // 如果昵称为空，则使用用户名作为后备选项
            usernameText.setText(user.getUsername());
        }
        
        emailText.setText(user.getEmail());
        
        // 更新健康数据
        if (user.getGender() != null) {
            genderText.setText(user.getGender());
        }
        
        if (user.getAge() != null) {
            ageText.setText(user.getAge() + "岁");
        }
        
        if (user.getHeight() != null) {
            heightText.setText(user.getHeight() + " cm");
        }
        
        if (user.getWeight() != null) {
            weightText.setText(user.getWeight() + " kg");
        }
        
        // 计算BMI
        if (user.getHeight() != null && user.getWeight() != null) {
            double height = user.getHeight() / 100.0; // 厘米转米
            double weight = user.getWeight();
            double bmi = calculateBMI(height, weight);
            updateBMIDisplay(bmi);
        }
    }
    
    private double calculateBMI(double heightInMeters, double weightInKg) {
        return weightInKg / (heightInMeters * heightInMeters);
    }
    
    private void updateBMIDisplay(double bmi) {
        bmiText.setText(df.format(bmi));
        
        // 设置BMI状态
        String status;
        int progress;
        int colorRes;
        
        if (bmi < 18.5) { // 偏瘦
            status = "(偏瘦)";
            progress = 15;
            colorRes = android.R.color.holo_blue_light;
        } else if (bmi < 24.0) { // 正常
            status = "(正常)";
            progress = 50;
            colorRes = android.R.color.holo_green_light;
        } else if (bmi < 28.0) { // 超重
            status = "(超重)";
            progress = 75;
            colorRes = android.R.color.holo_orange_light;
        } else { // 肥胖
            status = "(肥胖)";
            progress = 90;
            colorRes = android.R.color.holo_red_light;
        }
        
        bmiStatusText.setText(status);
        bmiProgressIndicator.setProgress(progress);
        bmiProgressIndicator.setIndicatorColor(requireContext().getResources().getColor(colorRes));
    }

    private void setupListeners() {
        // 添加空检查，防止NPE
        if (editProfileButton != null) {
            editProfileButton.setOnClickListener(v -> showEditProfileDialog());
        }
        
        if (updateHealthDataButton != null) {
            updateHealthDataButton.setOnClickListener(v -> showHealthDataUpdateDialog());
        }
        
        if (settingsText != null) {
            settingsText.setOnClickListener(v ->
                Toast.makeText(requireContext(), "设置", Toast.LENGTH_SHORT).show()
            );
        }
        
        if (healthRecordsText != null) {
            healthRecordsText.setOnClickListener(v ->
                Toast.makeText(requireContext(), "健康记录历史", Toast.LENGTH_SHORT).show()
            );
        }
        
        if (aboutText != null) {
            aboutText.setOnClickListener(v ->
                Toast.makeText(requireContext(), "关于我们", Toast.LENGTH_SHORT).show()
            );
        }
        
        if (logoutText != null) {
            logoutText.setOnClickListener(v -> {
                // 清除登录状态
                if (sharedPreferences != null) {
                    sharedPreferences.edit().clear().apply();
                }
                Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();
                // 这里可以跳转到登录页面
            });
        }
    }
    
    /**
     * 显示编辑个人资料对话框
     */
    private void showEditProfileDialog() {
        Log.d(TAG, "显示编辑个人资料对话框");
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
        
        // 获取对话框中的输入控件
        TextInputEditText nicknameInput = dialogView.findViewById(R.id.input_nickname);
        TextInputEditText emailInput = dialogView.findViewById(R.id.input_email);
        
        // 设置当前值
        if (currentUser != null) {
            nicknameInput.setText(currentUser.getNickname());
            emailInput.setText(currentUser.getEmail());
        }
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_profile)
            .setView(dialogView)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                // 获取输入的数据
                String nickname = nicknameInput.getText() != null ? nicknameInput.getText().toString() : "";
                String email = emailInput.getText() != null ? emailInput.getText().toString() : "";
                
                // 验证数据
                boolean isValid = true;
                
                if (nickname.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.nickname_empty, Toast.LENGTH_SHORT).show();
                    isValid = false;
                }
                
                if (email.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.email_empty, Toast.LENGTH_SHORT).show();
                    isValid = false;
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(requireContext(), R.string.email_invalid, Toast.LENGTH_SHORT).show();
                    isValid = false;
                }
                
                if (isValid && currentUser != null) {
                    // 创建用于更新的用户对象，只包含必要信息
                    User profileUser = new User();
                    profileUser.setId(currentUser.getId());
                    profileUser.setNickname(nickname);
                    profileUser.setEmail(email);
                    
                    // 调用ViewModel专用方法更新基本资料
                    userViewModel.updateUserProfile(profileUser);
                    
                    // 更新当前用户对象中的相应字段
                    currentUser.setNickname(nickname);
                    currentUser.setEmail(email);
                    
                    // 立即更新UI
                    updateUI(currentUser);
                    
                    Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    private void showHealthDataUpdateDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_update_health_data, null);
        
        // 获取对话框中的输入控件
        TextInputEditText genderInput = dialogView.findViewById(R.id.input_gender);
        TextInputEditText ageInput = dialogView.findViewById(R.id.input_age);
        TextInputEditText heightInput = dialogView.findViewById(R.id.input_height);
        TextInputEditText weightInput = dialogView.findViewById(R.id.input_weight);
        
        // 设置当前值
        if (currentUser != null) {
            genderInput.setText(currentUser.getGender());
            if (currentUser.getAge() != null) {
                ageInput.setText(String.valueOf(currentUser.getAge()));
            }
            if (currentUser.getHeight() != null) {
                heightInput.setText(String.valueOf(currentUser.getHeight()));
            }
            if (currentUser.getWeight() != null) {
                weightInput.setText(String.valueOf(currentUser.getWeight()));
            }
        }
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_basic_info)
            .setView(dialogView)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                // 获取输入的数据
                String gender = genderInput.getText() != null ? genderInput.getText().toString() : "";
                String ageStr = ageInput.getText() != null ? ageInput.getText().toString() : "";
                String heightStr = heightInput.getText() != null ? heightInput.getText().toString() : "";
                String weightStr = weightInput.getText() != null ? weightInput.getText().toString() : "";
                
                // 验证并保存数据
                boolean isValid = true;
                
                if (gender.isEmpty()) {
                    isValid = false;
                }
                
                Integer age = null;
                try {
                    if (!ageStr.isEmpty()) {
                        age = Integer.parseInt(ageStr);
                        if (age <= 0 || age > 120) {
                            isValid = false;
                            Toast.makeText(requireContext(), "年龄数据无效", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (NumberFormatException e) {
                    isValid = false;
                    Toast.makeText(requireContext(), "年龄必须是数字", Toast.LENGTH_SHORT).show();
                }
                
                Double height = null;
                try {
                    if (!heightStr.isEmpty()) {
                        height = Double.parseDouble(heightStr);
                        if (height <= 0 || height > 250) {
                            isValid = false;
                            Toast.makeText(requireContext(), "身高数据无效", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (NumberFormatException e) {
                    isValid = false;
                    Toast.makeText(requireContext(), "身高必须是数字", Toast.LENGTH_SHORT).show();
                }
                
                Double weight = null;
                try {
                    if (!weightStr.isEmpty()) {
                        weight = Double.parseDouble(weightStr);
                        if (weight <= 0 || weight > 500) {
                            isValid = false;
                            Toast.makeText(requireContext(), "体重数据无效", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (NumberFormatException e) {
                    isValid = false;
                    Toast.makeText(requireContext(), "体重必须是数字", Toast.LENGTH_SHORT).show();
                }
                
                if (isValid) {
                    // 创建用于更新的用户对象，只包含健康数据
                    User healthDataUser = new User();
                    if (currentUser != null) {
                        healthDataUser.setId(currentUser.getId());
                        healthDataUser.setGender(gender);
                        healthDataUser.setAge(age);
                        healthDataUser.setHeight(height);
                        healthDataUser.setWeight(weight);
                        
                        // 调用ViewModel专用方法更新健康数据
                        userViewModel.updateHealthData(healthDataUser);
                        
                        // 更新当前用户对象中的相应字段
                        currentUser.setGender(gender);
                        currentUser.setAge(age);
                        currentUser.setHeight(height);
                        currentUser.setWeight(weight);
                        
                        // 立即更新UI
                        updateUI(currentUser);
                        
                        Toast.makeText(requireContext(), R.string.basic_info_updated, Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    /**
     * 显示重试加载的Snackbar
     */
    private void showRetrySnackbar() {
        View rootView = getView();
        if (rootView != null) {
            Snackbar.make(rootView, "网络连接失败，是否重试？", Snackbar.LENGTH_INDEFINITE)
                .setAction("重试", v -> {
                    // 重新加载用户数据
                    long userId = sharedPreferences.getLong("user_id", -1);
                    if (userId != -1) {
                        loadingState(true); // 显示加载状态
                        userViewModel.getUserById(userId);
                    }
                })
                .show();
        }
    }
    
    /**
     * 从缓存加载用户数据
     */
    private User loadUserFromCache() {
        try {
            return userViewModel.getCachedUser();
        } catch (Exception e) {
            Log.e(TAG, "从缓存加载用户数据失败", e);
            return null;
        }
    }
    
    /**
     * 显示或隐藏加载状态
     */
    private void loadingState(boolean isLoading) {
        // 这里可以添加加载指示器的显示和隐藏逻辑
        if (getView() != null) {
            ProgressBar progressBar = getView().findViewById(R.id.progress_loading);
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        }
    }
} 