package com.healthx.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.healthx.R;

/**
 * 步数输入对话框
 */
public class StepInputDialog extends DialogFragment {
    
    private StepInputListener listener;
    private EditText stepsEditText;
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        // 加载自定义布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_step_input, null);
        
        // 初始化视图
        stepsEditText = view.findViewById(R.id.edit_text_steps);
        Button submitButton = view.findViewById(R.id.button_submit);
        Button cancelButton = view.findViewById(R.id.button_cancel);
        
        // 设置按钮点击事件
        submitButton.setOnClickListener(v -> onSubmitClicked());
        cancelButton.setOnClickListener(v -> dismiss());
        
        // 设置对话框标题和视图
        builder.setTitle("手动记录步数")
               .setView(view);
        
        return builder.create();
    }
    
    private void onSubmitClicked() {
        try {
            // 获取输入的步数
            String stepsStr = stepsEditText.getText().toString().trim();
            if (stepsStr.isEmpty()) {
                Toast.makeText(requireContext(), "请输入步数", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int steps = Integer.parseInt(stepsStr);
            if (steps <= 0) {
                Toast.makeText(requireContext(), "步数必须大于0", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (steps > 100000) {
                Toast.makeText(requireContext(), "步数不能超过100000", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 回调传递步数
            if (listener != null) {
                listener.onStepInputSubmit(steps);
            }
            
            // 关闭对话框
            dismiss();
            
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "请输入有效的步数", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 设置步数输入监听器
     * @param listener 监听器
     */
    public void setListener(StepInputListener listener) {
        this.listener = listener;
    }
    
    /**
     * 步数输入监听接口
     */
    public interface StepInputListener {
        void onStepInputSubmit(int steps);
    }
} 