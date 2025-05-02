package com.healthx.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.healthx.ui.fragment.HealthPlanFragment;
import com.healthx.ui.fragment.VisualizationFragment;

public class AnalysisPagerAdapter extends FragmentStateAdapter {

    public AnalysisPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new VisualizationFragment();
            case 1:
                return new HealthPlanFragment();
            default:
                return new VisualizationFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
} 