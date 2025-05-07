package com.healthx.ui.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.healthx.R;
import com.healthx.model.SleepRecord;
import com.healthx.util.PreferenceManager;
import com.healthx.viewmodel.SleepViewModel;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

public class SleepRecordDialog extends DialogFragment {
    
    private static final String ARG_SLEEP_RECORD = "arg_sleep_record";
    
    private TextView tvStartTime;
    private TextView tvEndTime;
    private Button btnSave;
    private Button btnCancel;
    private Button btnDelete;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SleepRecord existingRecord;
    
    private SleepRecordDialogListener listener;
    private SleepViewModel viewModel;
    
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    public interface SleepRecordDialogListener {
        void onSleepRecordSaved(LocalDateTime startTime, LocalDateTime endTime, SleepRecord existingRecord);
        void onSleepRecordDeleted(SleepRecord sleepRecord);
    }
    
    public static SleepRecordDialog newInstance(SleepRecord sleepRecord) {
        SleepRecordDialog dialog = new SleepRecordDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SLEEP_RECORD, sleepRecord);
        dialog.setArguments(args);
        return dialog;
    }
    
    public void setListener(SleepRecordDialogListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert);
        
        // 获取ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(SleepViewModel.class);
        
        // 设置用户ID
        long userId = PreferenceManager.getUserId(requireContext());
        viewModel.setUserId(userId);
        
        if (getArguments() != null && getArguments().containsKey(ARG_SLEEP_RECORD)) {
            existingRecord = (SleepRecord) getArguments().getSerializable(ARG_SLEEP_RECORD);
            startTime = existingRecord.getStartTime();
            endTime = existingRecord.getEndTime();
        } else {
            // 默认设置为昨晚22:00到今早6:00
            LocalDate yesterday = LocalDate.now().minusDays(1);
            startTime = LocalDateTime.of(yesterday, LocalTime.of(22, 0));
            endTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 0));
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_sleep_record, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        tvStartTime = view.findViewById(R.id.tvStartTime);
        tvEndTime = view.findViewById(R.id.tvEndTime);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnDelete = view.findViewById(R.id.btnDelete);
        
        // 显示时间
        updateTimeDisplay();
        
        // 设置点击事件
        tvStartTime.setOnClickListener(v -> showStartTimePickerDialog());
        tvEndTime.setOnClickListener(v -> showEndTimePickerDialog());
        
        btnSave.setOnClickListener(v -> saveSleepRecord());
        btnCancel.setOnClickListener(v -> dismiss());
        
        // 仅当编辑现有记录时才显示删除按钮
        if (existingRecord != null) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> deleteSleepRecord());
        } else {
            btnDelete.setVisibility(View.GONE);
        }
    }
    
    private void updateTimeDisplay() {
        tvStartTime.setText(startTime.format(timeFormatter));
        tvEndTime.setText(endTime.format(timeFormatter));
    }
    
    private void showStartTimePickerDialog() {
        TimePickerDialog dialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    // 更新开始时间，保持日期不变
                    startTime = LocalDateTime.of(startTime.toLocalDate(), LocalTime.of(hourOfDay, minute));
                    updateTimeDisplay();
                },
                startTime.getHour(),
                startTime.getMinute(),
                true
        );
        dialog.show();
    }
    
    private void showEndTimePickerDialog() {
        TimePickerDialog dialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    // 更新结束时间，如果结束时间早于开始时间，则认为是第二天
                    LocalTime endLocalTime = LocalTime.of(hourOfDay, minute);
                    LocalDate endDate = endTime.toLocalDate();
                    
                    // 如果结束时间早于开始时间，并且是同一天，则结束时间设为第二天
                    if (endLocalTime.isBefore(startTime.toLocalTime()) && 
                        endDate.isEqual(startTime.toLocalDate())) {
                        endDate = endDate.plusDays(1);
                    }
                    
                    endTime = LocalDateTime.of(endDate, endLocalTime);
                    updateTimeDisplay();
                },
                endTime.getHour(),
                endTime.getMinute(),
                true
        );
        dialog.show();
    }
    
    private void saveSleepRecord() {
        // 验证时间
        if (endTime.isBefore(startTime)) {
            Toast.makeText(getContext(), "结束时间不能早于开始时间", Toast.LENGTH_SHORT).show();
            return;
        }
        
        long durationMinutes = Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes > 24 * 60) { // 超过24小时
            Toast.makeText(getContext(), "睡眠时长不能超过24小时", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 获取睡眠记录的日期（使用睡眠开始时间的日期）
        LocalDate sleepDate = startTime.toLocalDate();
        
        // 检查该日期是否已有睡眠记录（除非是编辑现有记录）
        if (existingRecord == null) {
            // 使用异步方式检查
            viewModel.checkDateHasRecordAsync(sleepDate, null).observe(getViewLifecycleOwner(), hasRecord -> {
                if (hasRecord) {
                    Toast.makeText(getContext(), "该日期已有睡眠记录，不能重复添加", Toast.LENGTH_SHORT).show();
                } else {
                    // 没有重复记录，继续保存
                    saveRecord();
                }
            });
        } else {
            // 编辑现有记录，直接保存
            saveRecord();
        }
    }
    
    // 实际保存记录的方法
    private void saveRecord() {
        if (listener != null) {
            listener.onSleepRecordSaved(startTime, endTime, existingRecord);
        }
        
        dismiss();
    }
    
    private void deleteSleepRecord() {
        if (listener != null && existingRecord != null) {
            listener.onSleepRecordDeleted(existingRecord);
        }
        
        dismiss();
    }
} 