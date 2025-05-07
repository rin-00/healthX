package com.healthx.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.healthx.R;
import com.healthx.model.SleepRecord;
import com.healthx.util.DateTimeUtils;

import org.threeten.bp.format.DateTimeFormatter;

public class SleepRecordAdapter extends ListAdapter<SleepRecord, SleepRecordAdapter.SleepRecordViewHolder> {
    
    // 使用规范中定义的标准格式
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeUtils.UI_DATE_FORMAT;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeUtils.UI_TIME_FORMAT;
    
    private final OnSleepRecordClickListener clickListener;
    
    public interface OnSleepRecordClickListener {
        void onSleepRecordClick(SleepRecord sleepRecord);
    }
    
    public SleepRecordAdapter(OnSleepRecordClickListener clickListener) {
        super(new SleepRecordDiffCallback());
        this.clickListener = clickListener;
    }
    
    @NonNull
    @Override
    public SleepRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sleep_record, parent, false);
        return new SleepRecordViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SleepRecordViewHolder holder, int position) {
        SleepRecord sleepRecord = getItem(position);
        holder.bind(sleepRecord, clickListener);
    }
    
    static class SleepRecordViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate;
        private final TextView tvTimeRange;
        private final TextView tvDuration;
        private final TextView tvQuality;
        
        public SleepRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvQuality = itemView.findViewById(R.id.tvQuality);
        }
        
        public void bind(SleepRecord sleepRecord, OnSleepRecordClickListener listener) {
            // 显示日期
            tvDate.setText(sleepRecord.getStartTime().format(DATE_FORMATTER));
            
            // 显示时间范围
            String startTimeStr = sleepRecord.getStartTime().format(TIME_FORMATTER);
            String endTimeStr = sleepRecord.getEndTime().format(TIME_FORMATTER);
            tvTimeRange.setText(startTimeStr + " - " + endTimeStr);
            
            // 显示时长
            int durationMinutes = sleepRecord.getDuration();
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;
            tvDuration.setText(String.format("%d小时%d分钟", hours, minutes));
            
            // 显示质量（根据时长判断）
            String quality;
            if (durationMinutes < 360) { // 少于6小时
                quality = "不足";
            } else if (durationMinutes >= 360 && durationMinutes <= 480) { // 6-8小时
                quality = "良好";
            } else if (durationMinutes > 480 && durationMinutes <= 540) { // 8-9小时
                quality = "优秀";
            } else { // 超过9小时
                quality = "过量";
            }
            tvQuality.setText(quality);
            
            // 设置点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSleepRecordClick(sleepRecord);
                }
            });
        }
    }
    
    private static class SleepRecordDiffCallback extends DiffUtil.ItemCallback<SleepRecord> {
        @Override
        public boolean areItemsTheSame(@NonNull SleepRecord oldItem, @NonNull SleepRecord newItem) {
            return oldItem.getId() == newItem.getId();
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull SleepRecord oldItem, @NonNull SleepRecord newItem) {
            return oldItem.getStartTime().equals(newItem.getStartTime()) &&
                   oldItem.getEndTime().equals(newItem.getEndTime()) &&
                   oldItem.getDuration() == newItem.getDuration();
        }
    }
} 