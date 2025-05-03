package com.healthx;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.healthx.ui.fragment.AnalysisFragment;
import com.healthx.ui.fragment.ProfileFragment;
import com.healthx.ui.fragment.RecordFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BottomNavigationView navView;
    
    // Fragment对象
    private RecordFragment recordFragment;
    private AnalysisFragment analysisFragment;
    private ProfileFragment profileFragment;
    
    // 当前显示的Fragment
    private Fragment activeFragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Log.d(TAG, "onCreate: 开始初始化MainActivity");
        
        // 初始化Fragment管理器
        fragmentManager = getSupportFragmentManager();
        
        // 初始化底部导航
        setupBottomNavigation();
        
        // 初始化Fragment
        if (savedInstanceState == null) {
            setupFragments();
        }
    }
    
    private void setupBottomNavigation() {
        navView = findViewById(R.id.nav_view);
        if (navView == null) {
            Log.e(TAG, "导航视图未找到！");
            return;
        }
        
        Log.d(TAG, "找到导航视图，设置监听器");
        
        // 设置导航项选择监听器
        navView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "导航项被选中: " + itemId);
            
            if (itemId == R.id.navigation_record) {
                Log.d(TAG, "切换到记录页面");
                switchToFragment(recordFragment);
                return true;
            } else if (itemId == R.id.navigation_analysis) {
                Log.d(TAG, "切换到分析页面");
                switchToFragment(analysisFragment);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                Log.d(TAG, "切换到我的页面");
                switchToFragment(profileFragment);
                return true;
            }
            return false;
        });
    }
    
    private void setupFragments() {
        Log.d(TAG, "初始化Fragment");
        
        // 创建Fragment实例
        recordFragment = new RecordFragment();
        analysisFragment = new AnalysisFragment();
        profileFragment = new ProfileFragment();
        
        // 首次加载添加所有Fragment，并隐藏未选中的
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // 添加记录页面Fragment
        transaction.add(R.id.nav_host_fragment_container, recordFragment, "record");
        activeFragment = recordFragment;
        
        // 添加其他Fragment并隐藏
        transaction.add(R.id.nav_host_fragment_container, analysisFragment, "analysis").hide(analysisFragment);
        transaction.add(R.id.nav_host_fragment_container, profileFragment, "profile").hide(profileFragment);
        
        // 提交事务
        transaction.commit();
        
        Log.d(TAG, "Fragment初始化完成，默认显示记录页面");
    }
    
    private void switchToFragment(Fragment fragment) {
        if (fragment != null && fragment != activeFragment) {
            Log.d(TAG, "切换Fragment: " + fragment.getClass().getSimpleName());
            
            // 创建切换事务
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.hide(activeFragment).show(fragment);
            
            // 设置过渡动画
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            
            // 提交事务
            transaction.commit();
            
            // 更新当前活动Fragment
            activeFragment = fragment;
            
            Log.d(TAG, "Fragment切换完成");
        } else {
            Log.d(TAG, "无需切换Fragment或Fragment为null");
        }
    }

    @Override
    public void onBackPressed() {
        // 如果当前Fragment是RecordFragment，尝试处理子Fragment的返回
        if (activeFragment instanceof RecordFragment) {
            RecordFragment recordFrag = (RecordFragment) activeFragment;
            if (recordFrag.handleBackPress()) {
                // 已由RecordFragment处理
                return;
            }
        }
        
        // 默认处理
        super.onBackPressed();
    }
}