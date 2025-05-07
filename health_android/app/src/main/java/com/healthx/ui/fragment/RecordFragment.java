package com.healthx.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.healthx.R;
import com.healthx.model.HealthCard;
import com.healthx.ui.adapter.HealthCardAdapter;
import com.healthx.ui.fragment.DietFragment;
import com.healthx.ui.fragment.ExerciseFragment;
import com.healthx.ui.fragment.SleepDetailFragment;
import com.healthx.ui.fragment.SleepFragment;
import com.healthx.ui.fragment.StepFragment;
import com.healthx.ui.weight.WeightFragment;

import java.util.ArrayList;
import java.util.List;

public class RecordFragment extends Fragment implements HealthCardAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private HealthCardAdapter adapter;
    private FloatingActionButton fabAddRecord;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        setupListeners();
        setupBackStackListener();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_health_cards);
        fabAddRecord = view.findViewById(R.id.fab_add_record);
    }

    private void setupRecyclerView() {
        adapter = new HealthCardAdapter(getHealthCards());
        adapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAddRecord.setOnClickListener(v -> 
            Toast.makeText(requireContext(), "添加记录", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupBackStackListener() {
        getChildFragmentManager().addOnBackStackChangedListener(() -> {
            // 当返回栈变化时，检查是否需要恢复视图
            if (getChildFragmentManager().getBackStackEntryCount() == 0) {
                // 返回栈为空，恢复RecordFragment视图
                if (recyclerView != null) {
                    recyclerView.setVisibility(View.VISIBLE);
                }
                if (fabAddRecord != null) {
                    fabAddRecord.setVisibility(View.VISIBLE);
                }
                
                View fragmentContainer = requireView().findViewById(R.id.fragment_container);
                if (fragmentContainer != null) {
                    fragmentContainer.setVisibility(View.GONE);
                }
            }
        });
    }

    private List<HealthCard> getHealthCards() {
        // 模拟数据，实际应从ViewModel获取
        List<HealthCard> cards = new ArrayList<>();
        cards.add(new HealthCard(R.string.diet_record, "今天摄入了1800千卡", android.R.drawable.ic_menu_edit));
        cards.add(new HealthCard(R.string.exercise_record, "今天运动消耗了300千卡", android.R.drawable.ic_menu_compass));
        cards.add(new HealthCard(R.string.sleep_record, "昨晚睡眠7小时30分钟", android.R.drawable.ic_menu_recent_history));
        cards.add(new HealthCard(R.string.weight_record, "体重：65kg", android.R.drawable.ic_menu_report_image));
        cards.add(new HealthCard(R.string.step_record, "今日步数：8500步", android.R.drawable.ic_menu_directions));
        return cards;
    }
    
    @Override
    public void onItemClick(HealthCard healthCard) {
        int titleResId = healthCard.getTitleResId();
        if (titleResId == R.string.diet_record) {
            // 跳转到饮食记录页面
            navigateToDietFragment();
        } else if (titleResId == R.string.exercise_record) {
            // 跳转到运动记录页面
            navigateToExerciseFragment();
        } else if (titleResId == R.string.sleep_record) {
            // 跳转到睡眠记录页面
            navigateToSleepFragment();
        } else if (titleResId == R.string.weight_record) {
            // 跳转到体重记录页面
            navigateToWeightFragment();
        } else if (titleResId == R.string.step_record) {
            // 跳转到步数记录页面
            navigateToStepFragment();
        } else {
            // 处理其他类型的记录
            Toast.makeText(requireContext(), "点击了：" + getString(titleResId), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navigateToDietFragment() {
        // 隐藏RecyclerView和FAB
        recyclerView.setVisibility(View.GONE);
        fabAddRecord.setVisibility(View.GONE);
        
        // 显示Fragment容器
        View fragmentContainer = requireView().findViewById(R.id.fragment_container);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        // 添加DietFragment
        DietFragment dietFragment = new DietFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, dietFragment);
        transaction.addToBackStack("diet_fragment");
        transaction.commitAllowingStateLoss();
        
        // 确保事务完成后，视图可见性正确设置
        getChildFragmentManager().executePendingTransactions();
    }
    
    private void navigateToExerciseFragment() {
        // 隐藏RecyclerView和FAB
        recyclerView.setVisibility(View.GONE);
        fabAddRecord.setVisibility(View.GONE);
        
        // 显示Fragment容器
        View fragmentContainer = requireView().findViewById(R.id.fragment_container);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        // 添加ExerciseFragment
        ExerciseFragment exerciseFragment = new ExerciseFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, exerciseFragment);
        transaction.addToBackStack("exercise_fragment");
        transaction.commitAllowingStateLoss();
        
        // 确保事务完成后，视图可见性正确设置
        getChildFragmentManager().executePendingTransactions();
    }
    
    private void navigateToSleepFragment() {
        // 隐藏RecyclerView和FAB
        recyclerView.setVisibility(View.GONE);
        fabAddRecord.setVisibility(View.GONE);
        
        // 显示Fragment容器
        View fragmentContainer = requireView().findViewById(R.id.fragment_container);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        // 直接添加SleepDetailFragment，跳过SleepFragment
        SleepDetailFragment sleepDetailFragment = new SleepDetailFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, sleepDetailFragment, "sleep_detail_fragment");
        transaction.addToBackStack("sleep_detail_fragment");
        transaction.commitAllowingStateLoss();
        
        // 确保事务完成后，视图可见性正确设置
        getChildFragmentManager().executePendingTransactions();
    }
    
    private void navigateToWeightFragment() {
        // 隐藏RecyclerView和FAB
        recyclerView.setVisibility(View.GONE);
        fabAddRecord.setVisibility(View.GONE);
        
        // 显示Fragment容器
        View fragmentContainer = requireView().findViewById(R.id.fragment_container);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        // 添加WeightFragment
        WeightFragment weightFragment = new WeightFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, weightFragment);
        transaction.addToBackStack("weight_fragment");
        transaction.commitAllowingStateLoss();
        
        // 确保事务完成后，视图可见性正确设置
        getChildFragmentManager().executePendingTransactions();
    }
    
    private void navigateToStepFragment() {
        // 隐藏RecyclerView和FAB
        recyclerView.setVisibility(View.GONE);
        fabAddRecord.setVisibility(View.GONE);
        
        // 显示Fragment容器
        View fragmentContainer = requireView().findViewById(R.id.fragment_container);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        // 添加StepFragment
        StepFragment stepFragment = new StepFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, stepFragment);
        transaction.addToBackStack("step_fragment");
        transaction.commitAllowingStateLoss();
        
        // 确保事务完成后，视图可见性正确设置
        getChildFragmentManager().executePendingTransactions();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 如果返回到此Fragment，确保RecyclerView可见
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
        if (fabAddRecord != null) {
            fabAddRecord.setVisibility(View.VISIBLE);
        }
    }
    
    // 处理返回键，供MainActivity调用
    public boolean handleBackPress() {
        // 处理返回键
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            // 如果有子Fragment，处理返回栈
            getChildFragmentManager().popBackStackImmediate();
            
            // 确保视图可见性正确设置
            recyclerView.setVisibility(View.VISIBLE);
            fabAddRecord.setVisibility(View.VISIBLE);
            
            View fragmentContainer = requireView().findViewById(R.id.fragment_container);
            if (fragmentContainer != null) {
                fragmentContainer.setVisibility(View.GONE);
            }
            
            return true;
        }
        return false;
    }
} 