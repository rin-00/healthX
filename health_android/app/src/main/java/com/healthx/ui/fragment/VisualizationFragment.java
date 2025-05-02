package com.healthx.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.healthx.R;

import java.util.ArrayList;
import java.util.List;

public class VisualizationFragment extends Fragment {

    private LineChart dietChart;
    private BarChart exerciseChart;
    private LineChart weightChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visualization, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initCharts(view);
        setupChartData();
    }

    private void initCharts(View view) {
        dietChart = view.findViewById(R.id.chart_diet);
        exerciseChart = view.findViewById(R.id.chart_exercise);
        weightChart = view.findViewById(R.id.chart_weight);
    }

    private void setupChartData() {
        // 饮食图表
        List<Entry> dietEntries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            // 模拟一周的卡路里摄入数据
            dietEntries.add(new Entry(i, (float) (1500 + Math.random() * 1000)));
        }

        LineDataSet dietDataSet = new LineDataSet(dietEntries, "卡路里摄入");
        dietDataSet.setColor(Color.BLUE);
        dietDataSet.setCircleColor(Color.BLUE);
        dietDataSet.setLineWidth(2f);
        dietDataSet.setCircleRadius(4f);
        dietDataSet.setDrawValues(false);

        LineData dietData = new LineData(dietDataSet);
        dietChart.setData(dietData);
        dietChart.getDescription().setEnabled(false);
        dietChart.invalidate();

        // 运动图表
        List<BarEntry> exerciseEntries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            // 模拟一周的运动消耗数据
            exerciseEntries.add(new BarEntry(i, (float) (200 + Math.random() * 500)));
        }

        BarDataSet exerciseDataSet = new BarDataSet(exerciseEntries, "卡路里消耗");
        exerciseDataSet.setColor(Color.GREEN);
        exerciseDataSet.setDrawValues(false);

        BarData exerciseData = new BarData(exerciseDataSet);
        exerciseChart.setData(exerciseData);
        exerciseChart.getDescription().setEnabled(false);
        exerciseChart.invalidate();

        // 体重图表
        List<Entry> weightEntries = new ArrayList<>();
        float baseWeight = 65.0f;
        for (int i = 0; i < 7; i++) {
            // 模拟一周的体重数据
            weightEntries.add(new Entry(i, (float) (baseWeight - 0.1 * i + Math.random() * 0.5 - 0.25)));
        }

        LineDataSet weightDataSet = new LineDataSet(weightEntries, "体重(kg)");
        weightDataSet.setColor(Color.RED);
        weightDataSet.setCircleColor(Color.RED);
        weightDataSet.setLineWidth(2f);
        weightDataSet.setCircleRadius(4f);
        weightDataSet.setDrawValues(false);

        LineData weightData = new LineData(weightDataSet);
        weightChart.setData(weightData);
        weightChart.getDescription().setEnabled(false);
        weightChart.invalidate();
    }
} 