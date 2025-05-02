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

public class HealthPlanFragment extends Fragment {

    private Button generatePlanButton;
    private TextView healthPlanSummary;
    private TextView dietAdvice;
    private TextView exerciseAdvice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_health_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        generatePlanButton = view.findViewById(R.id.button_generate_plan);
        healthPlanSummary = view.findViewById(R.id.text_health_plan_summary);
        dietAdvice = view.findViewById(R.id.text_diet_advice);
        exerciseAdvice = view.findViewById(R.id.text_exercise_advice);
    }

    private void setupListeners() {
        generatePlanButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "正在生成健康计划...", Toast.LENGTH_SHORT).show();
            // 模拟网络请求延迟
            v.postDelayed(() -> {
                healthPlanSummary.setText("根据您最近的健康数据，我们为您定制了以下健康计划。");
                dietAdvice.setText("建议每日摄入蛋白质80g，碳水化合物250g，脂肪50g。多食用蔬菜水果，每日至少摄入5种不同颜色的蔬菜。");
                exerciseAdvice.setText("建议每周进行3次有氧运动，每次30分钟以上。另外增加2次力量训练，重点锻炼核心肌群和下肢肌肉。");
                Toast.makeText(requireContext(), "健康计划已生成", Toast.LENGTH_SHORT).show();
            }, 1500);
        });
    }
} 