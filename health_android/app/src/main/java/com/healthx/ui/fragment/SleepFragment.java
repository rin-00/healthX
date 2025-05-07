package com.healthx.ui.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.healthx.R;
import com.healthx.model.SleepRecord;
import com.healthx.repository.Resource;
import com.healthx.ui.adapter.SleepRecordAdapter;
import com.healthx.ui.dialog.SleepRecordDialog;
import com.healthx.util.DateTimeUtils;
import com.healthx.util.PreferenceManager;
import com.healthx.viewmodel.SleepViewModel;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SleepFragment extends Fragment implements SleepRecordDialog.SleepRecordDialogListener {
    
    private static final String TAG = "SleepFragment";
    
    private SleepViewModel viewModel;
    private RecyclerView recyclerView;
    private SleepRecordAdapter adapter;
    private BarChart sleepChart;
    private TextView tvNoSleepData;
    private TextView tvAverageSleepDuration;
    private TextView tvTodaySleepDuration;
    private TextView tvTodaySleepTime;
    private TextView tvTodaySleepQuality;
    private MaterialCardView cardTodaySleep;
    private Button btnAddSleep;
    private FloatingActionButton fabAddSleep;
    private Button btnSeeAllRecords;
    
    // 使用规范中定义的标准格式
    private DateTimeFormatter dateFormatter = DateTimeUtils.UI_DATE_FORMAT;
    private DateTimeFormatter timeFormatter = DateTimeUtils.UI_TIME_FORMAT;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sleep, container, false);
        
        // 初始化视图
        recyclerView = view.findViewById(R.id.recyclerView);
        sleepChart = view.findViewById(R.id.sleepChart);
        tvNoSleepData = view.findViewById(R.id.tvNoSleepData);
        tvAverageSleepDuration = view.findViewById(R.id.tvAverageSleepDuration);
        tvTodaySleepDuration = view.findViewById(R.id.tvTodaySleepDuration);
        tvTodaySleepTime = view.findViewById(R.id.tvTodaySleepTime);
        tvTodaySleepQuality = view.findViewById(R.id.tvTodaySleepQuality);
        cardTodaySleep = view.findViewById(R.id.cardTodaySleep);
        btnAddSleep = view.findViewById(R.id.btnAddSleep);
        fabAddSleep = view.findViewById(R.id.fabAddSleep);
        btnSeeAllRecords = view.findViewById(R.id.btnSeeAllRecords);
        
        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SleepRecordAdapter(record -> {
            // 点击记录时显示编辑对话框
            showEditSleepDialog(record);
        });
        recyclerView.setAdapter(adapter);
        
        // 设置图表属性
        setupChart();
        
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 获取ViewModel
        viewModel = new ViewModelProvider(this).get(SleepViewModel.class);
        
        // 设置用户ID
        long userId = PreferenceManager.getUserId(requireContext());
        viewModel.setUserId(userId);
        
        // 观察睡眠记录数据
        observeData();
        
        // 设置按钮点击事件
        btnAddSleep.setOnClickListener(v -> showAddSleepDialog());
        fabAddSleep.setOnClickListener(v -> showAddSleepDialog());
        
        // 设置查看所有记录按钮的点击事件
        btnSeeAllRecords.setOnClickListener(v -> {
            navigateToSleepDetailFragment();
        });
        
        // 找到睡眠统计卡片并设置点击事件
        View sleepStatsCard = view.findViewById(R.id.cardSleepStats);
        if (sleepStatsCard != null) {
            sleepStatsCard.setOnClickListener(v -> {
                navigateToSleepDetailFragment();
            });
        }
    }
    
    private void observeData() {
        // 先强制同步数据，确保本地数据与服务器一致
        refreshData();
        
        // 观察今日睡眠记录
        viewModel.getTodaySleepRecord().observe(getViewLifecycleOwner(), sleepRecord -> {
            updateTodaySleepCard(sleepRecord);
            
            // 根据是否有今日记录来控制FAB的显示
            if (sleepRecord != null) {
                fabAddSleep.setVisibility(View.GONE);
            } else {
                fabAddSleep.setVisibility(View.VISIBLE);
            }
        });
        
        // 观察最近7天睡眠记录
        viewModel.getLast7DaysSleepRecords().observe(getViewLifecycleOwner(), sleepRecords -> {
            if (sleepRecords != null && !sleepRecords.isEmpty()) {
                tvNoSleepData.setVisibility(View.GONE);
                sleepChart.setVisibility(View.VISIBLE);
                
                // 处理重复记录的问题，确保每天只显示一条记录
                List<SleepRecord> uniqueRecords = deduplicateSleepRecords(sleepRecords);
                adapter.submitList(uniqueRecords);
                updateChart(uniqueRecords);
                
                // 更新平均睡眠时长
                tvAverageSleepDuration.setText(viewModel.getAverageSleepDurationText());
            } else {
                tvNoSleepData.setVisibility(View.VISIBLE);
                sleepChart.setVisibility(View.GONE);
                adapter.submitList(new ArrayList<>());
                tvAverageSleepDuration.setText("未记录");
            }
        });
    }
    
    /**
     * 强制刷新数据，从服务器同步最新数据
     */
    private void refreshData() {
        long userId = PreferenceManager.getUserId(requireContext());
        viewModel.syncData().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                // 同步成功后，重新加载本地数据
                viewModel.reloadData();
            }
        });
    }
    
    /**
     * 去除重复的睡眠记录，确保每天只显示一条记录
     * @param records 原始记录列表
     * @return 去重后的记录列表
     */
    private List<SleepRecord> deduplicateSleepRecords(List<SleepRecord> records) {
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String, SleepRecord> recordMap = new HashMap<>();
        
        // 按日期分组，保留每天最新的记录
        for (SleepRecord record : records) {
            if (record.getStartTime() != null) {
                String dateKey = record.getStartTime().toLocalDate().toString();
                
                // 如果该日期没有记录，或当前记录的ID比已有记录大（说明更新），则更新
                if (!recordMap.containsKey(dateKey) || 
                    (record.getRemoteId() != null && 
                     recordMap.get(dateKey).getRemoteId() != null && 
                     record.getRemoteId() > recordMap.get(dateKey).getRemoteId())) {
                    recordMap.put(dateKey, record);
                }
            }
        }
        
        // 将Map转换回List并按日期排序
        List<SleepRecord> uniqueRecords = new ArrayList<>(recordMap.values());
        Collections.sort(uniqueRecords, (a, b) -> 
            b.getStartTime().toLocalDate().compareTo(a.getStartTime().toLocalDate()));
        
        return uniqueRecords;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次页面恢复时刷新数据
        refreshData();
    }
    
    private void updateTodaySleepCard(SleepRecord sleepRecord) {
        if (sleepRecord != null) {
            cardTodaySleep.setVisibility(View.VISIBLE);
            btnAddSleep.setVisibility(View.GONE);
            // 同时隐藏悬浮添加按钮
            fabAddSleep.setVisibility(View.GONE);
            
            tvTodaySleepDuration.setText(viewModel.getSleepDurationText(sleepRecord));
            tvTodaySleepTime.setText(viewModel.getSleepTimeRangeText(sleepRecord));
            tvTodaySleepQuality.setText(viewModel.getSleepQuality(sleepRecord));
            
            // 为睡眠卡片添加点击事件
            cardTodaySleep.setOnClickListener(v -> {
                navigateToSleepDetailFragment();
            });
        } else {
            cardTodaySleep.setVisibility(View.GONE);
            btnAddSleep.setVisibility(View.VISIBLE);
            // 显示悬浮添加按钮
            fabAddSleep.setVisibility(View.VISIBLE);
        }
    }
    
    private void setupChart() {
        // 设置图表基本属性
        sleepChart.getDescription().setEnabled(false);
        sleepChart.setDrawGridBackground(false);
        sleepChart.setDrawBarShadow(false);
        sleepChart.setHighlightPerTapEnabled(false);
        sleepChart.setMaxVisibleValueCount(7);
        sleepChart.setPinchZoom(false);
        sleepChart.setDrawValueAboveBar(true);
        
        XAxis xAxis = sleepChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        
        sleepChart.getAxisLeft().setAxisMinimum(0f);
        sleepChart.getAxisRight().setEnabled(false);
        sleepChart.getLegend().setEnabled(false);
    }
    
    private void updateChart(List<SleepRecord> sleepRecords) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        List<LocalDate> last7Days = viewModel.getLast7Days();
        List<Integer> durations = viewModel.getLast7DaysSleepDurations();
        
        for (int i = 0; i < last7Days.size(); i++) {
            // 将分钟转换为小时
            float hours = durations.get(i) / 60f;
            entries.add(new BarEntry(i, hours));
            
            // 格式化日期标签
            labels.add(last7Days.get(i).format(DateTimeFormatter.ofPattern("MM/dd")));
        }
        
        BarDataSet dataSet = new BarDataSet(entries, "睡眠时长（小时）");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        
        BarData data = new BarData(dataSet);
        data.setValueTextSize(10f);
        data.setBarWidth(0.7f);
        
        sleepChart.setData(data);
        sleepChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        sleepChart.invalidate();
    }
    
    private void showAddSleepDialog() {
        // 显示添加睡眠记录对话框
        SleepRecordDialog dialog = new SleepRecordDialog();
        dialog.setListener(this);
        dialog.show(getParentFragmentManager(), "SleepRecordDialog");
    }
    
    private void showEditSleepDialog(SleepRecord sleepRecord) {
        // 显示编辑睡眠记录对话框
        SleepRecordDialog dialog = SleepRecordDialog.newInstance(sleepRecord);
        dialog.setListener(this);
        dialog.show(getParentFragmentManager(), "SleepRecordDialog");
    }
    
    @Override
    public void onSleepRecordSaved(LocalDateTime startTime, LocalDateTime endTime, SleepRecord existingRecord) {
        if (existingRecord == null) {
            // 添加新记录，不需要额外检查，因为SleepRecordDialog已经进行了检查
            // 添加新记录
            viewModel.addSleepRecord(startTime, endTime).observe(getViewLifecycleOwner(), resource -> {
                switch (resource.getStatus()) {
                    case SUCCESS:
                        Toast.makeText(getContext(), "睡眠记录添加成功", Toast.LENGTH_SHORT).show();
                        // 如果添加的是今天的记录，则隐藏添加按钮
                        if (startTime.toLocalDate().isEqual(LocalDate.now())) {
                            fabAddSleep.setVisibility(View.GONE);
                            btnAddSleep.setVisibility(View.GONE);
                        }
                        break;
                    case ERROR:
                        Toast.makeText(getContext(), "添加失败: " + resource.getMessage(), Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        } else {
            // 更新现有记录
            existingRecord.setStartTime(startTime);
            existingRecord.setEndTime(endTime);
            
            viewModel.updateSleepRecord(existingRecord).observe(getViewLifecycleOwner(), resource -> {
                switch (resource.getStatus()) {
                    case SUCCESS:
                        Toast.makeText(getContext(), "睡眠记录更新成功", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        Toast.makeText(getContext(), "更新失败: " + resource.getMessage(), Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        }
    }
    
    @Override
    public void onSleepRecordDeleted(SleepRecord sleepRecord) {
        viewModel.deleteSleepRecord(sleepRecord).observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    Toast.makeText(getContext(), "睡眠记录删除成功", Toast.LENGTH_SHORT).show();
                    // 如果删除的是今天的记录，则显示添加按钮
                    if (sleepRecord.getStartTime() != null && 
                        sleepRecord.getStartTime().toLocalDate().isEqual(LocalDate.now())) {
                        fabAddSleep.setVisibility(View.VISIBLE);
                        btnAddSleep.setVisibility(View.VISIBLE);
                        cardTodaySleep.setVisibility(View.GONE);
                    }
                    break;
                case ERROR:
                    Toast.makeText(getContext(), "删除失败: " + resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
    
    // 新增导航方法
    private void navigateToSleepDetailFragment() {
        // 隐藏当前页面内容
        View mainContent = requireView().findViewById(R.id.sleepMainContent);
        if (mainContent != null) {
            mainContent.setVisibility(View.GONE);
        }
        
        // 显示Fragment容器
        View fragmentContainer = requireView().findViewById(R.id.sleepFragmentContainer);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
        } else {
            // 如果没有找到容器视图，可能是布局问题
            Toast.makeText(requireContext(), "无法打开睡眠详情页面", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 添加SleepDetailFragment
        SleepDetailFragment sleepDetailFragment = new SleepDetailFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.sleepFragmentContainer, sleepDetailFragment);
        transaction.addToBackStack("sleep_detail_fragment");
        transaction.commitAllowingStateLoss();
        
        // 确保事务完成后，视图可见性正确设置
        getChildFragmentManager().executePendingTransactions();
    }
    
    // 处理返回键，供MainActivity调用
    public boolean handleBackPress() {
        // 处理返回键
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            // 如果有子Fragment，处理返回栈
            getChildFragmentManager().popBackStackImmediate();
            
            // 恢复主内容视图的可见性
            View mainContent = requireView().findViewById(R.id.sleepMainContent);
            if (mainContent != null) {
                mainContent.setVisibility(View.VISIBLE);
            }
            
            // 隐藏Fragment容器
            View fragmentContainer = requireView().findViewById(R.id.sleepFragmentContainer);
            if (fragmentContainer != null) {
                fragmentContainer.setVisibility(View.GONE);
            }
            
            return true;
        }
        return false;
    }
} 