package com.healthx.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.healthx.R;
import com.healthx.model.StepRecord;
import com.healthx.util.DateTimeUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 步数记录适配器，用于RecyclerView显示步数记录
 */
public class StepRecordAdapter extends RecyclerView.Adapter<StepRecordAdapter.ViewHolder> {
    
    private final Context context;
    private final List<StepRecord> records = new ArrayList<>();
    private final OnItemClickListener listener;
    private final DecimalFormat distanceFormat = new DecimalFormat("#,##0.0");
    private final DecimalFormat caloriesFormat = new DecimalFormat("#,##0.0");
    
    public StepRecordAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_step_record, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StepRecord record = records.get(position);
        
        // 设置日期
        holder.dateText.setText(DateTimeUtils.formatLocalDate(record.getRecordDate()));
        
        // 设置步数
        holder.stepsText.setText(String.valueOf(record.getStepCount()));
        
        // 设置距离
        if (record.getDistance() != null) {
            holder.distanceText.setText(
                    String.format("%s 米", distanceFormat.format(record.getDistance())));
        } else {
            holder.distanceText.setText("0.0 米");
        }
        
        // 设置卡路里
        if (record.getCaloriesBurned() != null) {
            holder.caloriesText.setText(
                    String.format("%s 千卡", caloriesFormat.format(record.getCaloriesBurned())));
        } else {
            holder.caloriesText.setText("0.0 千卡");
        }
        
        // 设置来源
        String source = record.getSource();
        if (source == null || source.isEmpty()) {
            source = "手动记录";
        }
        holder.sourceText.setText(source);
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(record);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return records.size();
    }
    
    /**
     * 更新数据
     * @param newRecords 新的步数记录列表
     */
    public void updateData(List<StepRecord> newRecords) {
        records.clear();
        if (newRecords != null) {
            records.addAll(newRecords);
        }
        notifyDataSetChanged();
    }
    
    /**
     * 添加数据
     * @param newRecords 要添加的步数记录列表
     */
    public void addData(List<StepRecord> newRecords) {
        if (newRecords != null) {
            int startPosition = records.size();
            records.addAll(newRecords);
            notifyItemRangeInserted(startPosition, newRecords.size());
        }
    }
    
    /**
     * ViewHolder类
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        
        final TextView dateText;
        final TextView stepsText;
        final TextView distanceText;
        final TextView caloriesText;
        final TextView sourceText;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.text_date);
            stepsText = itemView.findViewById(R.id.text_steps);
            distanceText = itemView.findViewById(R.id.text_distance);
            caloriesText = itemView.findViewById(R.id.text_calories);
            sourceText = itemView.findViewById(R.id.text_source);
        }
    }
    
    /**
     * 点击事件监听接口
     */
    public interface OnItemClickListener {
        void onItemClick(StepRecord record);
    }
} 