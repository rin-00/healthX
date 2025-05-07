package com.healthx.ui.weight;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.healthx.R;
import com.healthx.model.WeightRecord;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 体重记录对话框，用于添加和编辑体重记录
 */
public class WeightRecordDialog extends DialogFragment {
    
    private EditText etWeight;
    private EditText etNote;
    private TextView tvTitle;
    private TextView tvDate;
    private Button btnSave;
    private Button btnCancel;
    
    private WeightRecord existingRecord;
    private WeightRecordDialogListener listener;
    
    // 格式化器
    private final DecimalFormat weightFormat = new DecimalFormat("#0.0");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    
    /**
     * 默认构造函数，用于添加新记录
     */
    public WeightRecordDialog() {
        this.existingRecord = null;
    }
    
    /**
     * 带参数构造函数，用于编辑现有记录
     */
    public WeightRecordDialog(WeightRecord record) {
        this.existingRecord = record;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_weight_record, container, false);
        
        // 初始化控件
        initViews(view);
        
        // 如果是编辑现有记录，填充数据
        if (existingRecord != null) {
            tvTitle.setText(R.string.edit_weight_record);
            tvDate.setText(dateFormat.format(new Date(existingRecord.getMeasurementTime())));
            etWeight.setText(weightFormat.format(existingRecord.getWeight()));
            etNote.setText(existingRecord.getNote());
        } else {
            tvTitle.setText(R.string.add_weight_record);
            tvDate.setText(dateFormat.format(new Date()));
        }
        
        // 设置按钮点击事件
        btnSave.setOnClickListener(v -> onSaveClicked());
        btnCancel.setOnClickListener(v -> dismiss());
        
        return view;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
    
    /**
     * 初始化视图控件
     */
    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tv_dialog_title);
        tvDate = view.findViewById(R.id.tv_weight_date);
        etWeight = view.findViewById(R.id.et_weight);
        etNote = view.findViewById(R.id.et_weight_note);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
    }
    
    /**
     * 保存按钮点击处理
     */
    private void onSaveClicked() {
        // 获取输入的体重
        String weightStr = etWeight.getText().toString().trim();
        if (TextUtils.isEmpty(weightStr)) {
            Toast.makeText(getContext(), "请输入体重", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 解析体重值
        float weight;
        try {
            weight = Float.parseFloat(weightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "请输入有效的体重值", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 验证体重合理范围（例如30kg-300kg）
        if (weight < 30 || weight > 300) {
            Toast.makeText(getContext(), "请输入合理范围内的体重(30-300kg)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 获取备注
        String note = etNote.getText().toString().trim();
        
        // 通过接口回调通知数据保存
        if (listener != null) {
            listener.onSave(weight, note);
        }
        
        // 关闭对话框
        dismiss();
    }
    
    /**
     * 设置监听器
     */
    public void setListener(WeightRecordDialogListener listener) {
        this.listener = listener;
    }
    
    /**
     * 体重记录对话框监听器接口
     */
    public interface WeightRecordDialogListener {
        void onSave(float weight, String note);
    }
} 