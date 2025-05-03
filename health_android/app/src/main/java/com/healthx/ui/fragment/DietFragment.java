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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.healthx.R;
import com.healthx.model.Diet;
import com.healthx.ui.adapter.DietAdapter;
import com.healthx.ui.dialog.DietDialogFragment;
import com.healthx.viewmodel.DietViewModel;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

public class DietFragment extends Fragment implements DietAdapter.OnDietClickListener {

    private static final String TAG = "DietFragment";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private DietViewModel viewModel;
    private DietAdapter adapter;
    
    private Toolbar toolbar;
    private TextView tvDate;
    private TextView tvCaloriesValue;
    private TabLayout tabMealTypes;
    private RecyclerView recyclerDiets;
    private TextView tvEmptyView;
    private FloatingActionButton fabAddDiet;
    private ImageButton btnCalendar;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diet, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化视图
        initViews(view);
        
        // 设置工具栏
        setupToolbar();
        
        // 设置ViewModel
        viewModel = new ViewModelProvider(this).get(DietViewModel.class);
        
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
        tabMealTypes = view.findViewById(R.id.tab_meal_types);
        recyclerDiets = view.findViewById(R.id.recycler_diets);
        tvEmptyView = view.findViewById(R.id.tv_empty_view);
        fabAddDiet = view.findViewById(R.id.fab_add_diet);
        btnCalendar = view.findViewById(R.id.btn_calendar);
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
        adapter = new DietAdapter();
        adapter.setOnDietClickListener(this);
        recyclerDiets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerDiets.setAdapter(adapter);
    }
    
    private void setupTabLayout() {
        tabMealTypes.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        viewModel.loadDietsByMealType(1, "ALL");
                        break;
                    case 1:
                        viewModel.loadDietsByMealType(1, "BREAKFAST");
                        break;
                    case 2:
                        viewModel.loadDietsByMealType(1, "LUNCH");
                        break;
                    case 3:
                        viewModel.loadDietsByMealType(1, "DINNER");
                        break;
                    case 4:
                        viewModel.loadDietsByMealType(1, "SNACK");
                        break;
                }
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
        fabAddDiet.setOnClickListener(v -> showAddDietDialog());
        
        btnCalendar.setOnClickListener(v -> showDatePicker());
        
        // 添加下拉刷新功能，如果布局中有SwipeRefreshLayout
        if (getView() != null) {
            androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh = 
                getView().findViewById(R.id.swipe_refresh_layout);
            if (swipeRefresh != null) {
                swipeRefresh.setOnRefreshListener(() -> {
                    refreshDataFromServer();
                    swipeRefresh.setRefreshing(false);
                });
            }
        }
    }
    
    private void observeData() {
        // 观察当前日期
        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            String formattedDate = date.format(DATE_FORMATTER);
            tvDate.setText(formattedDate);
        });
        
        // 观察饮食记录列表
        viewModel.getDiets().observe(getViewLifecycleOwner(), diets -> {
            adapter.submitList(diets);
            updateEmptyView(diets);
        });
        
        // 观察今日总卡路里
        viewModel.getTotalCaloriesToday().observe(getViewLifecycleOwner(), calories -> {
            if (calories != null) {
                tvCaloriesValue.setText(String.format("%.0f 千卡", calories));
            } else {
                tvCaloriesValue.setText("0 千卡");
            }
        });
    }
    
    private void updateEmptyView(List<Diet> diets) {
        if (diets == null || diets.isEmpty()) {
            tvEmptyView.setVisibility(View.VISIBLE);
            recyclerDiets.setVisibility(View.GONE);
        } else {
            tvEmptyView.setVisibility(View.GONE);
            recyclerDiets.setVisibility(View.VISIBLE);
        }
    }
    
    private void showAddDietDialog() {
        try {
            DietDialogFragment dialogFragment = DietDialogFragment.newInstance(null);
            dialogFragment.setOnDietSavedListener(diet -> {
                viewModel.addDiet(diet);
                Toast.makeText(getContext(), "饮食记录已添加", Toast.LENGTH_SHORT).show();
            });
            dialogFragment.show(getChildFragmentManager(), "add_diet");
        } catch (Exception e) {
            // 记录异常并显示错误提示
            Log.e(TAG, "显示饮食对话框时出错: " + e.getMessage(), e);
            Toast.makeText(getContext(), "无法显示添加对话框，请稍后再试", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showEditDietDialog(Diet diet) {
        try {
            if (diet == null) {
                Log.e(TAG, "尝试编辑空的饮食记录");
                return;
            }
            
            DietDialogFragment dialogFragment = DietDialogFragment.newInstance(diet);
            dialogFragment.setOnDietSavedListener(updatedDiet -> {
                viewModel.updateDiet(updatedDiet);
                Toast.makeText(getContext(), "饮食记录已更新", Toast.LENGTH_SHORT).show();
            });
            dialogFragment.show(getChildFragmentManager(), "edit_diet");
        } catch (Exception e) {
            // 记录异常并显示错误提示
            Log.e(TAG, "显示编辑对话框时出错: " + e.getMessage(), e);
            Toast.makeText(getContext(), "无法显示编辑对话框，请稍后再试", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showDeleteDietDialog(Diet diet) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除记录")
                .setMessage("确定要删除这条饮食记录吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    viewModel.deleteDiet(diet);
                    Toast.makeText(getContext(), "饮食记录已删除", Toast.LENGTH_SHORT).show();
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
                    viewModel.loadDietsForDate(1, selectedDate);
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
            viewModel.refreshDietsFromServer(userId);
            Toast.makeText(getContext(), "正在从服务器刷新数据...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "刷新数据失败: " + e.getMessage(), e);
            Toast.makeText(getContext(), "刷新数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onDietClick(Diet diet) {
        showEditDietDialog(diet);
    }
    
    @Override
    public void onDietLongClick(Diet diet) {
        showDeleteDietDialog(diet);
    }
} 