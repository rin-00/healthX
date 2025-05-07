package com.healthx.ui.weight.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.healthx.R;
import com.healthx.model.WeightRecord;
import com.healthx.util.DateTimeUtils;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

/**
 * 体重记录历史列表适配器
 */
public class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.WeightViewHolder> {
    
    private List<WeightRecord> weightRecords;
    private final WeightRecordClickListener listener;
    private final DecimalFormat weightFormat = new DecimalFormat("#0.0");
    private final DecimalFormat bmiFormat = new DecimalFormat("#0.0");
    
    public WeightHistoryAdapter(List<WeightRecord> weightRecords, WeightRecordClickListener listener) {
        this.weightRecords = weightRecords;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weight_record, parent, false);
        return new WeightViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
        WeightRecord record = weightRecords.get(position);
        holder.bind(record);
    }
    
    @Override
    public int getItemCount() {
        return weightRecords.size();
    }
    
    public void setWeightRecords(List<WeightRecord> weightRecords) {
        this.weightRecords = weightRecords;
        notifyDataSetChanged();
    }
    
    /**
     * 体重记录ViewHolder
     */
    class WeightViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate;
        private final TextView tvWeight;
        private final TextView tvBmi;
        private final TextView tvBmiStatus;
        private final TextView tvNote;
        
        WeightViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_weight_date);
            tvWeight = itemView.findViewById(R.id.tv_weight_value);
            tvBmi = itemView.findViewById(R.id.tv_weight_bmi);
            tvBmiStatus = itemView.findViewById(R.id.tv_weight_bmi_status);
            tvNote = itemView.findViewById(R.id.tv_weight_note);
            
            // 设置点击事件
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onWeightRecordClick(weightRecords.get(position));
                }
            });
            
            // 设置长按事件
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onWeightRecordLongClick(weightRecords.get(position));
                    return true;
                }
                return false;
            });
        }
        
        /**
         * 绑定数据到视图
         */
        void bind(WeightRecord record) {
            // 设置日期
            tvDate.setText(formatDate(record.getMeasurementTime()));
            
            // 设置体重
            tvWeight.setText(String.format("%s kg", weightFormat.format(record.getWeight())));
            
            // 设置BMI和状态
            if (record.getBmi() > 0) {
                tvBmi.setText(String.format("BMI: %s", bmiFormat.format(record.getBmi())));
                tvBmiStatus.setText(record.getBmiStatus());
                tvBmiStatus.setVisibility(View.VISIBLE);
                
                // 根据BMI状态设置不同颜色
                setBmiStatusColor(record.getBmiStatus());
            } else {
                tvBmi.setText("");
                tvBmiStatus.setVisibility(View.GONE);
            }
            
            // 设置备注
            if (record.getNote() != null && !record.getNote().isEmpty()) {
                tvNote.setText(record.getNote());
                tvNote.setVisibility(View.VISIBLE);
            } else {
                tvNote.setVisibility(View.GONE);
            }
        }
        
        /**
         * 根据BMI状态设置颜色
         */
        private void setBmiStatusColor(String status) {
            int colorResId;
            
            switch (status) {
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
            
            tvBmiStatus.setTextColor(itemView.getContext().getResources().getColor(colorResId));
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
    }
    
    /**
     * 体重记录点击监听器接口
     */
    public interface WeightRecordClickListener {
        void onWeightRecordClick(WeightRecord record);
        void onWeightRecordLongClick(WeightRecord record);
    }
} 