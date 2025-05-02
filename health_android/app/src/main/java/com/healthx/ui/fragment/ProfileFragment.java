package com.healthx.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.healthx.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private TextView usernameText;
    private TextView emailText;
    private CircleImageView avatarImage;
    private Button editProfileButton;
    private TextView settingsText;
    private TextView aboutText;
    private TextView logoutText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupUserData();
        setupListeners();
    }

    private void initViews(View view) {
        usernameText = view.findViewById(R.id.text_username);
        emailText = view.findViewById(R.id.text_email);
        avatarImage = view.findViewById(R.id.image_avatar);
        editProfileButton = view.findViewById(R.id.button_edit_profile);
        settingsText = view.findViewById(R.id.text_settings);
        aboutText = view.findViewById(R.id.text_about);
        logoutText = view.findViewById(R.id.text_logout);
    }

    private void setupUserData() {
        // 模拟用户数据，实际应从ViewModel获取
        usernameText.setText("测试用户");
        emailText.setText("test@example.com");
    }

    private void setupListeners() {
        // 添加空检查，防止NPE
        if (editProfileButton != null) {
            editProfileButton.setOnClickListener(v -> 
                Toast.makeText(requireContext(), "编辑个人资料", Toast.LENGTH_SHORT).show()
            );
        }
        
        if (settingsText != null) {
            settingsText.setOnClickListener(v ->
                Toast.makeText(requireContext(), "设置", Toast.LENGTH_SHORT).show()
            );
        }
        
        if (aboutText != null) {
            aboutText.setOnClickListener(v ->
                Toast.makeText(requireContext(), "关于我们", Toast.LENGTH_SHORT).show()
            );
        }
        
        if (logoutText != null) {
            logoutText.setOnClickListener(v ->
                Toast.makeText(requireContext(), "退出登录", Toast.LENGTH_SHORT).show()
            );
        }
    }
} 