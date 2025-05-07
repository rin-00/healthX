package com.healthx.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.healthx.repository.DietRepository;
import com.healthx.repository.ExerciseRepository;
import com.healthx.repository.SleepRepository;
import com.healthx.repository.StepRepository;
import com.healthx.repository.UserRepository;
import com.healthx.repository.WeightRepository;

/**
 * ViewModel工厂类，用于创建ViewModel实例
 */
public class ViewModelFactory implements ViewModelProvider.Factory {
    
    private final Context context;
    
    public ViewModelFactory(Context context) {
        this.context = context;
    }
    
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserViewModel.class)) {
            return (T) new UserViewModel(new UserRepository(context));
        } else if (modelClass.isAssignableFrom(WeightViewModel.class)) {
            return (T) new WeightViewModel(new WeightRepository(context));
        } else if (modelClass.isAssignableFrom(DietViewModel.class)) {
            return (T) new DietViewModel(new DietRepository(context));
        } else if (modelClass.isAssignableFrom(ExerciseViewModel.class)) {
            return (T) new ExerciseViewModel(new ExerciseRepository(context));
        } else if (modelClass.isAssignableFrom(SleepViewModel.class)) {
            return (T) new SleepViewModel(new SleepRepository(context));
        } else if (modelClass.isAssignableFrom(StepViewModel.class)) {
            return (T) new StepViewModel(new StepRepository(context));
        }
        
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
} 