package com.healthx.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.healthx.R;
import com.healthx.ui.adapter.AnalysisPagerAdapter;

public class AnalysisFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private AnalysisPagerAdapter pagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analysis, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupViewPager();
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabs_analysis);
        viewPager = view.findViewById(R.id.viewpager_analysis);
    }

    private void setupViewPager() {
        pagerAdapter = new AnalysisPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.data_visualization);
                    break;
                case 1:
                    tab.setText(R.string.health_plan);
                    break;
            }
        }).attach();
    }
} 