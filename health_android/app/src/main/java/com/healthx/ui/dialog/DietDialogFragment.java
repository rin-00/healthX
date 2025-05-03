package com.healthx.ui.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.healthx.R;
import com.healthx.model.Diet;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.Calendar;

public class DietDialogFragment extends DialogFragment {
    
    private static final String ARG_DIET = "diet";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private EditText etFoodName;
    private EditText etCalories;
    private EditText etProtein;
    private EditText etCarbs;
    private EditText etFat;
    private Spinner spinnerMealType;
    private TextView tvDate;
    private TextView tvTime;
    private Button btnSave;
    private Button btnCancel;
    
    private LocalDate selectedDate = LocalDate.now();
    private LocalTime selectedTime = LocalTime.now();
    
    private Diet diet;
    private OnDietSavedListener listener;
    
    public static DietDialogFragment newInstance(Diet diet) {
        DietDialogFragment fragment = new DietDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DIET, diet);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            diet = (Diet) getArguments().getSerializable(ARG_DIET);
        }
        // 使用Material主题
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_diet, container, false);
        
        initViews(view);
        setupMealTypeSpinner();
        setupListeners();
        
        if (diet != null) {
            // 编辑模式，填充现有数据
            fillData();
        }
        
        return view;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
    
    private void initViews(View view) {
        etFoodName = view.findViewById(R.id.et_food_name);
        etCalories = view.findViewById(R.id.et_calories);
        etProtein = view.findViewById(R.id.et_protein);
        etCarbs = view.findViewById(R.id.et_carbs);
        etFat = view.findViewById(R.id.et_fat);
        spinnerMealType = view.findViewById(R.id.spinner_meal_type);
        tvDate = view.findViewById(R.id.tv_date);
        tvTime = view.findViewById(R.id.tv_time);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
        
        // 设置默认日期和时间
        tvDate.setText(selectedDate.format(DATE_FORMATTER));
        tvTime.setText(selectedTime.format(TIME_FORMATTER));
    }
    
    private void setupMealTypeSpinner() {
        String[] mealTypes = new String[]{"早餐", "午餐", "晚餐", "零食"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, mealTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealType.setAdapter(adapter);
    }
    
    private void setupListeners() {
        tvDate.setOnClickListener(v -> showDatePicker());
        tvTime.setOnClickListener(v -> showTimePicker());
        
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveDiet();
            }
        });
        
        btnCancel.setOnClickListener(v -> dismiss());
    }
    
    private void fillData() {
        etFoodName.setText(diet.getFoodName());
        etCalories.setText(String.valueOf(diet.getCalories()));
        etProtein.setText(String.valueOf(diet.getProtein()));
        etCarbs.setText(String.valueOf(diet.getCarbs()));
        etFat.setText(String.valueOf(diet.getFat()));
        
        // 设置餐次类型
        String mealType = diet.getMealType();
        int position = 0;
        switch (mealType) {
            case "BREAKFAST":
                position = 0;
                break;
            case "LUNCH":
                position = 1;
                break;
            case "DINNER":
                position = 2;
                break;
            case "SNACK":
                position = 3;
                break;
        }
        spinnerMealType.setSelection(position);
        
        // 设置日期和时间
        selectedDate = diet.getEatenAt().toLocalDate();
        selectedTime = diet.getEatenAt().toLocalTime();
        tvDate.setText(selectedDate.format(DATE_FORMATTER));
        tvTime.setText(selectedTime.format(TIME_FORMATTER));
    }
    
    private boolean validateInputs() {
        if (etFoodName.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "请输入食物名称", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (etCalories.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "请输入卡路里", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void saveDiet() {
        // 获取输入的数据
        String foodName = etFoodName.getText().toString().trim();
        double calories = Double.parseDouble(etCalories.getText().toString().trim());
        
        double protein = 0;
        if (!etProtein.getText().toString().trim().isEmpty()) {
            protein = Double.parseDouble(etProtein.getText().toString().trim());
        }
        
        double carbs = 0;
        if (!etCarbs.getText().toString().trim().isEmpty()) {
            carbs = Double.parseDouble(etCarbs.getText().toString().trim());
        }
        
        double fat = 0;
        if (!etFat.getText().toString().trim().isEmpty()) {
            fat = Double.parseDouble(etFat.getText().toString().trim());
        }
        
        // 获取餐次类型
        String mealTypeText = spinnerMealType.getSelectedItem().toString();
        String mealType;
        switch (mealTypeText) {
            case "早餐":
                mealType = "BREAKFAST";
                break;
            case "午餐":
                mealType = "LUNCH";
                break;
            case "晚餐":
                mealType = "DINNER";
                break;
            case "零食":
                mealType = "SNACK";
                break;
            default:
                mealType = "BREAKFAST";
        }
        
        // 创建日期时间
        LocalDateTime eatenAt = LocalDateTime.of(selectedDate, selectedTime);
        
        if (diet == null) {
            // 创建新的饮食记录
            diet = new Diet(1, foodName, calories, protein, carbs, fat, mealType, eatenAt);
        } else {
            // 更新现有的饮食记录
            diet.setFoodName(foodName);
            diet.setCalories(calories);
            diet.setProtein(protein);
            diet.setCarbs(carbs);
            diet.setFat(fat);
            diet.setMealType(mealType);
            diet.setEatenAt(eatenAt);
        }
        
        if (listener != null) {
            listener.onDietSaved(diet);
        }
        
        dismiss();
    }
    
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    tvDate.setText(selectedDate.format(DATE_FORMATTER));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectedTime.getHour());
        calendar.set(Calendar.MINUTE, selectedTime.getMinute());
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedTime = LocalTime.of(hourOfDay, minute);
                    tvTime.setText(selectedTime.format(TIME_FORMATTER));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        
        timePickerDialog.show();
    }
    
    public void setOnDietSavedListener(OnDietSavedListener listener) {
        this.listener = listener;
    }
    
    public interface OnDietSavedListener {
        void onDietSaved(Diet diet);
    }
} 