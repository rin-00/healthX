package com.healthx.ui.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.healthx.R;
import com.healthx.model.Exercise;
import com.healthx.ui.adapter.ExerciseAdapter;
import com.healthx.ui.dialog.ExerciseDialogFragment;
import com.healthx.viewmodel.ExerciseViewModel;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

public class ExerciseFragment extends Fragment implements ExerciseAdapter.OnExerciseClickListener {
    
    private static final String TAG = "ExerciseFragment";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private ExerciseViewModel viewModel;
    private ExerciseAdapter adapter;
    
    private Toolbar toolbar;
    private TextView tvDate;
    private TextView tvCaloriesValue;
    private TextView tvExerciseTimeValue;
    private TabLayout tabExerciseTypes;
    private RecyclerView recyclerExercises;
    private TextView tvEmptyView;
    private FloatingActionButton fabAddExercise;
    private ImageButton btnCalendar;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercise, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化视图
        initViews(view);
        
        // 设置工具栏
        setupToolbar();
        
        // 设置ViewModel
        viewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);
        
        // 从服务器刷新数据
        refreshDataFromServer();
        
        // 设置RecyclerView和适配器
        setupRecyclerView();
        
        // 设置TabLayout
        setupTabLayout();
        
        // 设置监听器
        setupListeners();
        
        // 观察数据变化
        observeData();
    }
    
    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        tvDate = view.findViewById(R.id.tv_date);
        tvCaloriesValue = view.findViewById(R.id.tv_calories_value);
        tvExerciseTimeValue = view.findViewById(R.id.tv_exercise_time_value);
        tabExerciseTypes = view.findViewById(R.id.tab_exercise_types);
        recyclerExercises = view.findViewById(R.id.recycler_exercises);
        tvEmptyView = view.findViewById(R.id.tv_empty_view);
        fabAddExercise = view.findViewById(R.id.fab_add_exercise);
        btnCalendar = view.findViewById(R.id.btn_calendar);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
    }
    
    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> {
            // 处理返回按钮点击事件，返回上一级
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
                
                // 确保父Fragment(RecordFragment)中的视图正确显示
                Fragment parentFragment = getParentFragment();
                if (parentFragment instanceof RecordFragment) {
                    RecordFragment recordFragment = (RecordFragment) parentFragment;
                    View recyclerView = parentFragment.getView().findViewById(R.id.recycler_health_cards);
                    View fabAddRecord = parentFragment.getView().findViewById(R.id.fab_add_record);
                    View fragmentContainer = parentFragment.getView().findViewById(R.id.fragment_container);
                    
                    if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
                    if (fabAddRecord != null) fabAddRecord.setVisibility(View.VISIBLE);
                    if (fragmentContainer != null) fragmentContainer.setVisibility(View.GONE);
                }
            }
        });
    }
    
    private void setupRecyclerView() {
        adapter = new ExerciseAdapter();
        adapter.setOnExerciseClickListener(this);
        recyclerExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerExercises.setAdapter(adapter);
    }
    
    private void setupTabLayout() {
        tabExerciseTypes.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String exerciseType;
                switch (tab.getPosition()) {
                    case 0:
                        exerciseType = "ALL";
                        break;
                    case 1:
                        exerciseType = "有氧运动";
                        break;
                    case 2:
                        exerciseType = "力量训练";
                        break;
                    case 3:
                        exerciseType = "柔韧性训练";
                        break;
                    case 4:
                        exerciseType = "平衡训练";
                        break;
                    default:
                        exerciseType = "ALL";
                        break;
                }
                
                viewModel.loadExercisesByType(1, exerciseType);
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 不需要处理
            }
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 不需要处理
            }
        });
    }
    
    private void setupListeners() {
        fabAddExercise.setOnClickListener(v -> showAddExerciseDialog());
        
        btnCalendar.setOnClickListener(v -> showDatePicker());
        
        // 设置下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshDataFromServer();
            swipeRefreshLayout.setRefreshing(false);
        });
    }
    
    private void observeData() {
        // 观察当前日期
        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            String formattedDate = date.format(DATE_FORMATTER);
            tvDate.setText(formattedDate);
        });
        
        // 观察运动记录列表
        viewModel.getExercises().observe(getViewLifecycleOwner(), exercises -> {
            adapter.submitList(exercises);
            updateEmptyView(exercises);
            updateExerciseTimeValue(exercises);
        });
        
        // 观察今日总消耗卡路里
        viewModel.getTotalCaloriesToday().observe(getViewLifecycleOwner(), calories -> {
            if (calories != null) {
                tvCaloriesValue.setText(String.format("%.0f 千卡", calories));
            } else {
                tvCaloriesValue.setText("0 千卡");
            }
        });
    }
    
    private void updateEmptyView(List<Exercise> exercises) {
        if (exercises == null || exercises.isEmpty()) {
            tvEmptyView.setVisibility(View.VISIBLE);
            recyclerExercises.setVisibility(View.GONE);
        } else {
            tvEmptyView.setVisibility(View.GONE);
            recyclerExercises.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateExerciseTimeValue(List<Exercise> exercises) {
        if (exercises != null && !exercises.isEmpty()) {
            int totalDuration = 0;
            for (Exercise exercise : exercises) {
                totalDuration += exercise.getDuration();
            }
            tvExerciseTimeValue.setText(totalDuration + " 分钟");
        } else {
            tvExerciseTimeValue.setText("0 分钟");
        }
    }
    
    private void showAddExerciseDialog() {
        try {
            ExerciseDialogFragment dialogFragment = ExerciseDialogFragment.newInstance(null);
            dialogFragment.setOnExerciseSavedListener(exercise -> {
                viewModel.addExercise(exercise);
                Toast.makeText(getContext(), "运动记录已添加", Toast.LENGTH_SHORT).show();
            });
            dialogFragment.show(getChildFragmentManager(), "add_exercise");
        } catch (Exception e) {
            // 记录异常并显示错误提示
            Log.e(TAG, "显示运动对话框时出错: " + e.getMessage(), e);
            Toast.makeText(getContext(), "无法显示添加对话框，请稍后再试", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showEditExerciseDialog(Exercise exercise) {
        try {
            if (exercise == null) {
                Log.e(TAG, "尝试编辑空的运动记录");
                return;
            }
            
            ExerciseDialogFragment dialogFragment = ExerciseDialogFragment.newInstance(exercise);
            dialogFragment.setOnExerciseSavedListener(updatedExercise -> {
                viewModel.updateExercise(updatedExercise);
                Toast.makeText(getContext(), "运动记录已更新", Toast.LENGTH_SHORT).show();
            });
            dialogFragment.show(getChildFragmentManager(), "edit_exercise");
        } catch (Exception e) {
            // 记录异常并显示错误提示
            Log.e(TAG, "显示编辑对话框时出错: " + e.getMessage(), e);
            Toast.makeText(getContext(), "无法显示编辑对话框，请稍后再试", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showDeleteExerciseDialog(Exercise exercise) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除记录")
                .setMessage("确定要删除这条运动记录吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    viewModel.deleteExercise(exercise);
                    Toast.makeText(getContext(), "运动记录已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    private void showDatePicker() {
        LocalDate currentDate = viewModel.getSelectedDate().getValue();
        if (currentDate == null) {
            currentDate = LocalDate.now();
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(currentDate.getYear(), currentDate.getMonthValue() - 1, currentDate.getDayOfMonth());
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    viewModel.loadExercisesForDate(1, selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    /**
     * 从服务器刷新数据
     */
    private void refreshDataFromServer() {
        try {
            // 默认用户ID为1，实际应用中应该从会话获取
            long userId = 1; 
            viewModel.refreshExercisesFromServer(userId);
            Toast.makeText(getContext(), "正在从服务器刷新数据...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "刷新数据失败: " + e.getMessage(), e);
            Toast.makeText(getContext(), "刷新数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onExerciseClick(Exercise exercise) {
        showEditExerciseDialog(exercise);
    }
    
    @Override
    public void onExerciseLongClick(Exercise exercise) {
        showDeleteExerciseDialog(exercise);
    }
} 