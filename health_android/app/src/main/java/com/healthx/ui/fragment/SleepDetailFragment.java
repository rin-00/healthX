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
import androidx.lifecycle.ViewModelProvider;
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
import com.healthx.ui.adapter.SleepRecordAdapter;
import com.healthx.ui.dialog.SleepRecordDialog;
import com.healthx.util.DateTimeUtils;
import com.healthx.util.PreferenceManager;
import com.healthx.viewmodel.SleepViewModel;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class SleepDetailFragment extends Fragment implements SleepRecordAdapter.OnSleepRecordClickListener, SleepRecordDialog.SleepRecordDialogListener {

    private SleepViewModel viewModel;
    private RecyclerView recyclerView;
    private SleepRecordAdapter adapter;
    private BarChart sleepChart;
    private TextView tvNoSleepData;
    private TextView tvAverageSleepDuration;
    private FloatingActionButton fabAddSleep;
    
    private long userId;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd");
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SleepViewModel.class);
        userId = PreferenceManager.getUserId(requireContext());
        
        // 启用向上导航
        setHasOptionsMenu(true);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sleep_detail, container, false);
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 设置标题栏和返回按钮
        setupToolbar();
        
        // 初始化视图
        sleepChart = view.findViewById(R.id.sleepChart);
        tvNoSleepData = view.findViewById(R.id.tvNoSleepData);
        tvAverageSleepDuration = view.findViewById(R.id.tvAverageSleepDuration);
        recyclerView = view.findViewById(R.id.recyclerViewSleep);
        fabAddSleep = view.findViewById(R.id.fabAddSleep);
        
        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SleepRecordAdapter(this);
        recyclerView.setAdapter(adapter);
        
        // 设置图表
        setupChart();
        
        // 设置添加睡眠记录按钮
        fabAddSleep.setOnClickListener(v -> showAddSleepDialog());
        
        // 加载数据
        loadData();
    }
    
    private void setupToolbar() {
        // 查找顶部工具栏
        androidx.appcompat.widget.Toolbar toolbar = getView().findViewById(R.id.toolbar);
        if (toolbar != null) {
            // 如果是在子Fragment中，需要使用自己的工具栏
            androidx.appcompat.app.AppCompatActivity activity = (androidx.appcompat.app.AppCompatActivity) requireActivity();
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            activity.getSupportActionBar().setTitle("睡眠记录");
        }
    }
    
    private void setupChart() {
        // 配置图表
        sleepChart.getDescription().setEnabled(false);
        sleepChart.setDrawGridBackground(false);
        sleepChart.setDrawBarShadow(false);
        sleepChart.setDrawValueAboveBar(true);
        sleepChart.setPinchZoom(false);
        sleepChart.setDrawGridBackground(false);
        sleepChart.setMaxVisibleValueCount(7);
        
        // 配置X轴
        XAxis xAxis = sleepChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        
        // 配置左Y轴
        sleepChart.getAxisLeft().setDrawGridLines(true);
        sleepChart.getAxisLeft().setAxisMinimum(0f);
        
        // 配置右Y轴
        sleepChart.getAxisRight().setEnabled(false);
        
        // 配置图例
        sleepChart.getLegend().setEnabled(false);
    }
    
    private void loadData() {
        // 加载最近7天的睡眠记录
        viewModel.getLast7DaysSleepRecords(userId).observe(getViewLifecycleOwner(), sleepRecords -> {
            if (sleepRecords != null && !sleepRecords.isEmpty()) {
                tvNoSleepData.setVisibility(View.GONE);
                sleepChart.setVisibility(View.VISIBLE);
                tvAverageSleepDuration.setVisibility(View.VISIBLE);
                
                // 更新RecyclerView
                adapter.submitList(sleepRecords);
                
                // 更新图表
                updateChart(sleepRecords);
                
                // 计算平均睡眠时长
                calculateAverageSleepDuration(sleepRecords);
                
                // 检查今天是否已有睡眠记录，有则隐藏添加按钮
                checkTodayRecordAndUpdateButton(sleepRecords);
            } else {
                tvNoSleepData.setVisibility(View.VISIBLE);
                sleepChart.setVisibility(View.GONE);
                tvAverageSleepDuration.setVisibility(View.GONE);
                // 无记录时显示添加按钮
                fabAddSleep.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private void updateChart(List<SleepRecord> sleepRecords) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        
        // 准备最近7天的日期标签
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            labels.add(date.format(dateFormatter));
        }
        
        // 填充数据
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(6 - i);
            float sleepHours = 0f;
            
            // 查找该日期的睡眠记录
            for (SleepRecord record : sleepRecords) {
                LocalDate recordDate = record.getStartTime().toLocalDate();
                if (recordDate.equals(date)) {
                    sleepHours = record.getDuration() / 60f; // 转换分钟到小时
                    break;
                }
            }
            
            entries.add(new BarEntry(i, sleepHours));
        }
        
        // 创建数据集
        BarDataSet dataSet = new BarDataSet(entries, "睡眠时长（小时）");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setDrawValues(true);
        
        // 创建数据对象
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);
        
        // 设置X轴标签
        sleepChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        
        // 设置数据
        sleepChart.setData(data);
        sleepChart.invalidate();
    }
    
    private void calculateAverageSleepDuration(List<SleepRecord> sleepRecords) {
        if (sleepRecords.isEmpty()) return;
        
        int totalMinutes = 0;
        for (SleepRecord record : sleepRecords) {
            totalMinutes += record.getDuration();
        }
        
        float averageHours = totalMinutes / (60f * sleepRecords.size());
        int hours = (int) averageHours;
        int minutes = (int) ((averageHours - hours) * 60);
        
        tvAverageSleepDuration.setText(String.format("平均睡眠时长: %d小时%d分钟", hours, minutes));
    }
    
    private void showAddSleepDialog() {
        SleepRecordDialog dialog = new SleepRecordDialog();
        dialog.setListener(this);
        dialog.show(getParentFragmentManager(), "SleepRecordDialog");
    }
    
    @Override
    public void onSleepRecordClick(SleepRecord sleepRecord) {
        // 点击睡眠记录项，显示编辑对话框
        SleepRecordDialog dialog = SleepRecordDialog.newInstance(sleepRecord);
        dialog.setListener(this);
        dialog.show(getParentFragmentManager(), "SleepRecordDialog");
    }
    
    @Override
    public void onSleepRecordSaved(LocalDateTime startTime, LocalDateTime endTime, SleepRecord existingRecord) {
        if (existingRecord == null) {
            // 添加新记录，不需要额外检查，因为SleepRecordDialog已经进行了检查
            // 添加新记录
            viewModel.addSleepRecord(userId, startTime, endTime).observe(getViewLifecycleOwner(), resource -> {
                switch (resource.getStatus()) {
                    case SUCCESS:
                        Toast.makeText(getContext(), "睡眠记录添加成功", Toast.LENGTH_SHORT).show();
                        // 如果添加的是今天的记录，则隐藏添加按钮
                        if (startTime.toLocalDate().isEqual(LocalDate.now())) {
                            fabAddSleep.setVisibility(View.GONE);
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
                    }
                    break;
                case ERROR:
                    Toast.makeText(getContext(), "删除失败: " + resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
    
    // 检查今天是否已有睡眠记录，有则隐藏添加按钮
    private void checkTodayRecordAndUpdateButton(List<SleepRecord> sleepRecords) {
        LocalDate today = LocalDate.now();
        boolean hasTodayRecord = false;
        
        for (SleepRecord record : sleepRecords) {
            if (record.getStartTime() != null && 
                record.getStartTime().toLocalDate().isEqual(today)) {
                hasTodayRecord = true;
                break;
            }
        }
        
        // 根据是否有今日记录来控制FAB的显示
        if (hasTodayRecord) {
            fabAddSleep.setVisibility(View.GONE);
        } else {
            fabAddSleep.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 当点击返回按钮时，直接返回到记录界面
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 