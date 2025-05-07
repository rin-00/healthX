package com.healthx.ui.fragment;

import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.healthx.R;
import com.healthx.model.StepRecord;
import com.healthx.ui.adapter.StepRecordAdapter;
import com.healthx.ui.dialog.StepInputDialog;
import com.healthx.util.PreferenceManager;
import com.healthx.viewmodel.StepViewModel;
import com.healthx.viewmodel.ViewModelFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 步数统计Fragment
 */
public class StepFragment extends Fragment implements StepRecordAdapter.OnItemClickListener, StepInputDialog.StepInputListener {
    
    private StepViewModel viewModel;
    private PreferenceManager preferenceManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView todayStepsText;
    private TextView todayDistanceText;
    private TextView todayCaloriesText;
    private Button manualInputButton;
    private FloatingActionButton refreshButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private StepRecordAdapter adapter;
    private DecimalFormat distanceFormat = new DecimalFormat("#,##0.0");
    private DecimalFormat caloriesFormat = new DecimalFormat("#,##0.0");
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = PreferenceManager.getInstance(requireContext());
        ViewModelFactory factory = new ViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(StepViewModel.class);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化视图
        initViews(view);
        
        // 设置监听器
        setupListeners();
        
        // 观察数据变化
        observeViewModel();
        
        // 加载数据
        loadData(false);
    }
    
    private void initViews(View view) {
        todayStepsText = view.findViewById(R.id.text_today_steps);
        todayDistanceText = view.findViewById(R.id.text_today_distance);
        todayCaloriesText = view.findViewById(R.id.text_today_calories);
        manualInputButton = view.findViewById(R.id.button_manual_input);
        refreshButton = view.findViewById(R.id.fab_refresh);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        
        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new StepRecordAdapter(requireContext(), this);
        recyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        // 下拉刷新
        swipeRefreshLayout.setOnRefreshListener(() -> loadData(true));
        
        // 手动录入按钮
        manualInputButton.setOnClickListener(v -> showStepInputDialog());
        
        // 刷新按钮
        refreshButton.setOnClickListener(v -> loadData(true));
    }
    
    private void observeViewModel() {
        // 观察今日步数
        viewModel.getTodaySteps().observe(getViewLifecycleOwner(), steps -> {
            todayStepsText.setText(String.valueOf(steps));
        });
        
        // 观察今日距离
        viewModel.getTodayDistance().observe(getViewLifecycleOwner(), distance -> {
            if (distance != null) {
                todayDistanceText.setText(String.format("%s 米", distanceFormat.format(distance)));
            } else {
                todayDistanceText.setText("0.0 米");
            }
        });
        
        // 观察今日卡路里
        viewModel.getTodayCalories().observe(getViewLifecycleOwner(), calories -> {
            if (calories != null) {
                todayCaloriesText.setText(String.format("%s 千卡", caloriesFormat.format(calories)));
            } else {
                todayCaloriesText.setText("0.0 千卡");
            }
        });
        
        // 观察步数记录列表
        viewModel.getUserStepRecords(preferenceManager.getUserId()).observe(getViewLifecycleOwner(), records -> {
            adapter.updateData(records);
            swipeRefreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
        });
        
        // 观察加载状态
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        
        // 观察错误消息
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadData(boolean forceRefresh) {
        long userId = preferenceManager.getUserId();
        viewModel.getTodayStepRecord(userId, forceRefresh);
    }
    
    private void showStepInputDialog() {
        StepInputDialog dialog = new StepInputDialog();
        dialog.setListener(this);
        dialog.show(getChildFragmentManager(), "StepInputDialog");
    }
    
    @Override
    public void onItemClick(StepRecord record) {
        // 处理列表项点击事件，可以打开详情或编辑对话框
        Toast.makeText(requireContext(), "点击了 " + record.getRecordDate() + " 的记录", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onStepInputSubmit(int steps) {
        long userId = preferenceManager.getUserId();
        viewModel.recordSteps(userId, steps, "手动录入");
    }
} 