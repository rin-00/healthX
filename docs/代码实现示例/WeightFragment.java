package com.healthx.ui.weight;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.healthx.R;
import com.healthx.adapter.WeightRecordAdapter;
import com.healthx.databinding.FragmentWeightBinding;
import com.healthx.model.WeightRecord;
import com.healthx.util.SpacingItemDecoration;
import com.healthx.viewmodel.WeightViewModel;

/**
 * 体重记录Fragment - 重构后
 * 专注于同步功能的实现
 */
public class WeightFragment extends Fragment {
    
    private WeightViewModel viewModel;
    private FragmentWeightBinding binding;
    private WeightRecordAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // 启用选项菜单
        viewModel = new ViewModelProvider(this).get(WeightViewModel.class);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWeightBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupRecyclerView();
        setupSwipeRefresh();
        setupObservers();
        setupListeners();
    }
    
    /**
     * 设置RecyclerView
     */
    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new SpacingItemDecoration(getResources().getDimensionPixelSize(R.dimen.item_spacing)));
        
        adapter = new WeightRecordAdapter(new WeightRecordAdapter.WeightDiff());
        recyclerView.setAdapter(adapter);
        
        // 设置编辑和删除点击监听器
        adapter.setOnEditClickListener(this::showEditDialog);
        adapter.setOnDeleteClickListener(this::showDeleteConfirmDialog);
    }
    
    /**
     * 设置下拉刷新功能
     */
    private void setupSwipeRefresh() {
        swipeRefreshLayout = binding.swipeRefreshLayout;
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark
        );
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 从服务器刷新数据
            viewModel.refreshWeightRecordsFromServer();
            // 添加延迟，给用户刷新反馈
            new Handler().postDelayed(() -> {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 1500);
        });
    }
    
    /**
     * 设置LiveData观察者
     */
    private void setupObservers() {
        // 观察体重记录列表
        viewModel.getWeightRecords().observe(getViewLifecycleOwner(), weightRecords -> {
            adapter.submitList(weightRecords);
            binding.emptyView.setVisibility(weightRecords.isEmpty() ? View.VISIBLE : View.GONE);
        });
        
        // 观察加载状态
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });
        
        // 观察Toast消息
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 设置按钮点击监听器
     */
    private void setupListeners() {
        // 添加体重记录按钮
        binding.fabAddWeight.setOnClickListener(v -> {
            showAddDialog();
        });
    }
    
    /**
     * 显示添加体重记录对话框
     */
    private void showAddDialog() {
        // 实现添加体重记录的对话框...
    }
    
    /**
     * 显示编辑体重记录对话框
     */
    private void showEditDialog(WeightRecord record) {
        // 实现编辑体重记录的对话框...
    }
    
    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmDialog(WeightRecord record) {
        // 实现删除确认对话框...
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_weight, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    /**
     * 处理菜单项点击事件
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sync) {
            // 同步体重数据
            viewModel.syncWeightRecords();
            swipeRefreshLayout.setRefreshing(true);
            // 添加延迟，给用户同步反馈
            new Handler().postDelayed(() -> {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 1500);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 