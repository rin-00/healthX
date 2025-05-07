package com.healthx.ui.weight;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.healthx.R;
import com.healthx.model.WeightRecord;
import com.healthx.ui.weight.adapter.WeightHistoryAdapter;
import com.healthx.util.DateTimeUtils;
import com.healthx.viewmodel.WeightViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 体重管理主界面
 */
public class WeightFragment extends Fragment implements WeightHistoryAdapter.WeightRecordClickListener {
    
    private WeightViewModel viewModel;
    private WeightHistoryAdapter adapter;
    
    // UI组件
    private View weightOverview;
    private View emptyView;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddRecord;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    // 格式化器
    private final DecimalFormat weightFormat = new DecimalFormat("#0.0");
    private final DecimalFormat bmiFormat = new DecimalFormat("#0.0");
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true); // 启用选项菜单
        return inflater.inflate(R.layout.fragment_weight, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(WeightViewModel.class);
        
        // 初始化UI组件
        initViews(view);
        
        // 设置工具栏
        setupToolbar(view);
        
        // 设置RecyclerView
        setupRecyclerView();
        
        // 设置下拉刷新
        setupSwipeRefresh();
        
        // 监听LiveData变化
        observeViewModel();
    }
    
    private void initViews(View view) {
        weightOverview = view.findViewById(R.id.layout_weight_overview);
        emptyView = view.findViewById(R.id.layout_empty_state);
        recyclerView = view.findViewById(R.id.recycler_view_weight_history);
        fabAddRecord = view.findViewById(R.id.fab_add_weight);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        
        // 添加体重记录按钮点击事件
        fabAddRecord.setOnClickListener(v -> showAddWeightDialog());
    }
    
    /**
     * 设置工具栏和返回按钮
     */
    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                // 返回上一页
                getParentFragmentManager().popBackStack();
            });
        }
    }
    
    /**
     * 设置下拉刷新
     */
    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                    R.color.colorPrimary,
                    R.color.colorAccent,
                    R.color.colorPrimaryDark
            );
            
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // 同步数据
                syncData();
            });
        }
    }
    
    private void setupRecyclerView() {
        adapter = new WeightHistoryAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
    
    private void observeViewModel() {
        // 监听体重记录列表变化
        viewModel.getAllWeightRecords().observe(getViewLifecycleOwner(), weightRecords -> {
            adapter.setWeightRecords(weightRecords);
            
            // 根据是否有记录显示空视图
            if (weightRecords == null || weightRecords.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
            
            // 停止刷新动画
            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        
        // 监听最新体重变化
        viewModel.getLatestWeight().observe(getViewLifecycleOwner(), weight -> {
            if (weight != null && weight > 0) {
                // 在界面上更新最新体重显示
                updateWeightOverviewUI(weight, viewModel.getLatestBmi().getValue(), viewModel.getBmiStatus().getValue());
            }
        });
        
        // 监听最新BMI变化
        viewModel.getLatestBmi().observe(getViewLifecycleOwner(), bmi -> {
            if (bmi != null && bmi > 0) {
                // 在界面上更新BMI显示
                updateWeightOverviewUI(viewModel.getLatestWeight().getValue(), bmi, viewModel.getBmiStatus().getValue());
            }
        });
        
        // 监听BMI状态变化
        viewModel.getBmiStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                // 在界面上更新BMI状态显示
                updateWeightOverviewUI(viewModel.getLatestWeight().getValue(), viewModel.getLatestBmi().getValue(), status);
            }
        });
        
        // 监听加载状态
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // 控制加载指示器显示/隐藏
            if (isLoading) {
                showLoading();
            } else {
                hideLoading();
                // 停止刷新动画
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        
        // 监听提示消息
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 同步数据
     */
    private void syncData() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        
        viewModel.syncWeightRecords().observe(getViewLifecycleOwner(), resource -> {
            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_weight, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sync) {
            syncData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * 更新顶部体重概览区域UI
     */
    private void updateWeightOverviewUI(Float weight, Float bmi, String bmiStatus) {
        if (weightOverview == null) return;
        
        TextView tvLatestWeight = weightOverview.findViewById(R.id.tv_latest_weight);
        TextView tvBmiValue = weightOverview.findViewById(R.id.tv_bmi_value);
        TextView tvBmiStatus = weightOverview.findViewById(R.id.tv_bmi_status);
        
        // 更新体重显示
        if (weight != null && weight > 0) {
            tvLatestWeight.setText(String.format("%s kg", weightFormat.format(weight)));
        } else {
            tvLatestWeight.setText("-- kg");
        }
        
        // 更新BMI显示
        if (bmi != null && bmi > 0) {
            tvBmiValue.setText(String.format("%s", bmiFormat.format(bmi)));
            
            if (bmiStatus != null && !bmiStatus.isEmpty()) {
                tvBmiStatus.setText(bmiStatus);
                tvBmiStatus.setVisibility(View.VISIBLE);
                
                // 根据BMI状态设置颜色
                int colorResId;
                switch (bmiStatus) {
                    case "偏瘦":
                        colorResId = R.color.bmi_underweight;
                        break;
                    case "正常":
                        colorResId = R.color.bmi_normal;
                        break;
                    case "超重":
                        colorResId = R.color.bmi_overweight;
                        break;
                    case "肥胖":
                        colorResId = R.color.bmi_obese;
                        break;
                    default:
                        colorResId = R.color.black;
                        break;
                }
                tvBmiStatus.setTextColor(getResources().getColor(colorResId));
            } else {
                tvBmiStatus.setVisibility(View.GONE);
            }
        } else {
            tvBmiValue.setText("--");
            tvBmiStatus.setVisibility(View.GONE);
        }
    }
    
    /**
     * 显示添加体重记录对话框
     */
    private void showAddWeightDialog() {
        WeightRecordDialog dialog = new WeightRecordDialog();
        dialog.setListener(new WeightRecordDialog.WeightRecordDialogListener() {
            @Override
            public void onSave(float weight, String note) {
                viewModel.addWeightRecord(weight, note);
            }
        });
        dialog.show(getChildFragmentManager(), "AddWeightDialog");
    }
    
    /**
     * 显示编辑体重记录对话框
     */
    private void showEditWeightDialog(WeightRecord record) {
        WeightRecordDialog dialog = new WeightRecordDialog(record);
        dialog.setListener(new WeightRecordDialog.WeightRecordDialogListener() {
            @Override
            public void onSave(float weight, String note) {
                viewModel.updateWeightRecord(record, weight, note);
            }
        });
        dialog.show(getChildFragmentManager(), "EditWeightDialog");
    }
    
    /**
     * 删除体重记录
     */
    private void deleteWeightRecord(WeightRecord record) {
        // 可以在这里添加确认对话框
        viewModel.deleteWeightRecord(record);
    }
    
    /**
     * 显示加载指示器
     */
    private void showLoading() {
        View progressBar = getView().findViewById(R.id.progress_bar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 隐藏加载指示器
     */
    private void hideLoading() {
        View progressBar = getView().findViewById(R.id.progress_bar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }
    
    /**
     * 格式化日期为友好显示文本
     */
    private String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        
        // 如果是今天
        if (DateTimeUtils.isToday(date)) {
            return "今天 " + DateTimeUtils.formatTime(date);
        }
        
        // 如果是昨天
        if (DateTimeUtils.isYesterday(date)) {
            return "昨天 " + DateTimeUtils.formatTime(date);
        }
        
        // 其他日期
        return DateTimeUtils.formatDate(date) + " " + DateTimeUtils.formatTime(date);
    }
    
    // WeightHistoryAdapter.WeightRecordClickListener接口实现
    @Override
    public void onWeightRecordClick(WeightRecord record) {
        showEditWeightDialog(record);
    }
    
    @Override
    public void onWeightRecordLongClick(WeightRecord record) {
        // 长按显示操作菜单（例如删除）
        deleteWeightRecord(record);
    }
} 