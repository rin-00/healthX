package com.healthx;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupNavigation();
    }
    
    private void setupNavigation() {
        try {
            BottomNavigationView navView = findViewById(R.id.nav_view);
            
            // 设置顶部导航栏配置
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_record, R.id.navigation_analysis, R.id.navigation_profile)
                    .build();
            
            // 安全获取NavController
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
            
            if (navHostFragment != null) {
                navController = navHostFragment.getNavController();
                // 设置ActionBar
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
                // 设置底部导航
                NavigationUI.setupWithNavController(navView, navController);
            } else {
                Log.e(TAG, "Navigation host fragment not found!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}