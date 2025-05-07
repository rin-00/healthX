package com.healthx.ui.dialog;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.healthx.R;
import com.healthx.model.Exercise;
import com.healthx.util.ExerciseConstants;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Calendar;

public class ExerciseDialogFragment extends DialogFragment {
    
    private static final String ARG_EXERCISE = "arg_exercise";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private Exercise exercise;
    private OnExerciseSavedListener listener;
    
    private TextView tvDialogTitle;
    private TextInputEditText etExerciseName;
    private Spinner spinnerExerciseType;
    private Spinner spinnerIntensity;
    private TextInputEditText etDuration;
    private TextInputEditText etCalories;
    private Button btnCalculate;
    private Button btnDate;
    private Button btnTime;
    private Button btnCancel;
    private Button btnSave;
    
    private LocalDate selectedDate;
    private LocalTime selectedTime;
    
    // 保存当前选中的运动类型和强度
    private String currentExerciseType;
    private String currentIntensity;
    
    public static ExerciseDialogFragment newInstance(@Nullable Exercise exercise) {
        ExerciseDialogFragment fragment = new ExerciseDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EXERCISE, exercise);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert);
        
        if (getArguments() != null) {
            exercise = (Exercise) getArguments().getSerializable(ARG_EXERCISE);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_exercise, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupSpinners();
        setupListeners();
        
        // 如果是编辑模式，则填充数据
        if (exercise != null) {
            fillData();
        } else {
            // 如果是新增模式，则初始化为当前日期和时间
            selectedDate = LocalDate.now();
            selectedTime = LocalTime.now();
            updateDateButton();
            updateTimeButton();
        }
    }
    
    private void initViews(View view) {
        tvDialogTitle = view.findViewById(R.id.tv_dialog_title);
        etExerciseName = view.findViewById(R.id.et_exercise_name);
        spinnerExerciseType = view.findViewById(R.id.spinner_exercise_type);
        spinnerIntensity = view.findViewById(R.id.spinner_intensity);
        etDuration = view.findViewById(R.id.et_duration);
        etCalories = view.findViewById(R.id.et_calories);
        btnCalculate = view.findViewById(R.id.btn_calculate);
        btnDate = view.findViewById(R.id.btn_date);
        btnTime = view.findViewById(R.id.btn_time);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSave = view.findViewById(R.id.btn_save);
    }
    
    private void setupSpinners() {
        // 设置运动类型选项
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.exercise_types,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExerciseType.setAdapter(typeAdapter);
        
        // 设置强度选项
        ArrayAdapter<CharSequence> intensityAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.exercise_intensities,
                android.R.layout.simple_spinner_item
        );
        intensityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIntensity.setAdapter(intensityAdapter);
        
        // 保存初始选择的运动类型和强度
        currentExerciseType = spinnerExerciseType.getSelectedItem().toString();
        currentIntensity = spinnerIntensity.getSelectedItem().toString();
    }
    
    private void setupListeners() {
        // 设置自动计算按钮点击事件
        btnCalculate.setOnClickListener(v -> calculateCalories());
        
        // 监听运动类型选择
        spinnerExerciseType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentExerciseType = parent.getItemAtPosition(position).toString();
                // 如果已经输入了时长，可以自动计算卡路里
                if (!TextUtils.isEmpty(etDuration.getText())) {
                    calculateCalories();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 不处理
            }
        });
        
        // 监听强度选择
        spinnerIntensity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentIntensity = parent.getItemAtPosition(position).toString();
                // 如果已经输入了时长，可以自动计算卡路里
                if (!TextUtils.isEmpty(etDuration.getText())) {
                    calculateCalories();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 不处理
            }
        });
        
        // 监听时长输入变化
        etDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 不处理
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 不处理
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                // 时长变化后不自动计算卡路里，而是等用户点击计算按钮
            }
        });
        
        // 设置日期选择按钮点击事件
        btnDate.setOnClickListener(v -> showDatePicker());
        
        // 设置时间选择按钮点击事件
        btnTime.setOnClickListener(v -> showTimePicker());
        
        // 设置取消按钮点击事件
        btnCancel.setOnClickListener(v -> dismiss());
        
        // 设置保存按钮点击事件
        btnSave.setOnClickListener(v -> saveExercise());
    }
    
    /**
     * 自动计算卡路里消耗
     */
    private void calculateCalories() {
        String durationStr = etDuration.getText() != null ? etDuration.getText().toString().trim() : "";
        
        if (TextUtils.isEmpty(durationStr)) {
            etDuration.setError("请先输入运动时长");
            return;
        }
        
        try {
            int duration = Integer.parseInt(durationStr);
            if (duration <= 0) {
                etDuration.setError("运动时长必须大于0");
                return;
            }
            
            // 使用工具类计算卡路里消耗
            double calories = ExerciseConstants.calculateCaloriesBurned(
                    currentExerciseType, 
                    duration, 
                    currentIntensity
            );
            
            // 显示计算结果，保留1位小数
            etCalories.setText(String.format("%.1f", calories));
            
        } catch (NumberFormatException e) {
            etDuration.setError("请输入有效的时长");
        }
    }
    
    private void fillData() {
        tvDialogTitle.setText("编辑运动记录");
        
        etExerciseName.setText(exercise.getExerciseName());
        
        // 设置运动类型
        String exerciseType = exercise.getExerciseType();
        if (exerciseType != null && !exerciseType.isEmpty()) {
            ArrayAdapter adapter = (ArrayAdapter) spinnerExerciseType.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(exerciseType)) {
                    spinnerExerciseType.setSelection(i);
                    break;
                }
            }
        }
        
        // 设置强度
        String intensity = exercise.getIntensity();
        if (intensity != null && !intensity.isEmpty()) {
            ArrayAdapter adapter = (ArrayAdapter) spinnerIntensity.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(intensity)) {
                    spinnerIntensity.setSelection(i);
                    break;
                }
            }
        }
        
        // 设置时长和卡路里
        etDuration.setText(String.valueOf(exercise.getDuration()));
        etCalories.setText(String.format("%.1f", exercise.getCaloriesBurned()));
        
        // 设置日期和时间
        LocalDateTime exercisedAt = exercise.getExercisedAt();
        selectedDate = exercisedAt.toLocalDate();
        selectedTime = exercisedAt.toLocalTime();
        updateDateButton();
        updateTimeButton();
        
        // 更新当前选择的运动类型和强度
        currentExerciseType = exerciseType;
        currentIntensity = intensity;
    }
    
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDate != null) {
            calendar.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());
        }
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    updateDateButton();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedTime != null) {
            calendar.set(Calendar.HOUR_OF_DAY, selectedTime.getHour());
            calendar.set(Calendar.MINUTE, selectedTime.getMinute());
        }
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedTime = LocalTime.of(hourOfDay, minute);
                    updateTimeButton();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        
        timePickerDialog.show();
    }
    
    private void updateDateButton() {
        if (selectedDate != null) {
            btnDate.setText(selectedDate.format(DATE_FORMATTER));
        }
    }
    
    private void updateTimeButton() {
        if (selectedTime != null) {
            btnTime.setText(selectedTime.format(TIME_FORMATTER));
        }
    }
    
    private void saveExercise() {
        // 表单验证
        String exerciseName = etExerciseName.getText() != null ? etExerciseName.getText().toString().trim() : "";
        String durationStr = etDuration.getText() != null ? etDuration.getText().toString().trim() : "";
        String caloriesStr = etCalories.getText() != null ? etCalories.getText().toString().trim() : "";
        
        if (TextUtils.isEmpty(exerciseName)) {
            etExerciseName.setError("请输入运动名称");
            return;
        }
        
        if (TextUtils.isEmpty(durationStr)) {
            etDuration.setError("请输入运动时长");
            return;
        }
        
        if (TextUtils.isEmpty(caloriesStr)) {
            etCalories.setError("请输入消耗卡路里");
            return;
        }
        
        if (selectedDate == null || selectedTime == null) {
            Toast.makeText(requireContext(), "请选择运动日期和时间", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int duration = Integer.parseInt(durationStr);
        double calories = Double.parseDouble(caloriesStr);
        String exerciseType = spinnerExerciseType.getSelectedItem().toString();
        String intensity = spinnerIntensity.getSelectedItem().toString();
        
        // 创建或更新运动记录
        LocalDateTime exercisedAt = LocalDateTime.of(selectedDate, selectedTime);
        
        if (exercise == null) {
            // 创建新的运动记录
            exercise = new Exercise(1, exerciseName, duration, calories, exerciseType, intensity, exercisedAt);
        } else {
            // 更新现有的运动记录
            exercise.setExerciseName(exerciseName);
            exercise.setDuration(duration);
            exercise.setCaloriesBurned(calories);
            exercise.setExerciseType(exerciseType);
            exercise.setIntensity(intensity);
            exercise.setExercisedAt(exercisedAt);
        }
        
        if (listener != null) {
            listener.onExerciseSaved(exercise);
        }
        
        dismiss();
    }
    
    public void setOnExerciseSavedListener(OnExerciseSavedListener listener) {
        this.listener = listener;
    }
    
    public interface OnExerciseSavedListener {
        void onExerciseSaved(Exercise exercise);
    }
} 