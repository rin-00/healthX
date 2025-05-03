package com.healthx.viewmodel;

import android.content.Context;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * 自定义ViewModelFactory，用于传递参数给ViewModel
 */
public class ViewModelFactory implements ViewModelProvider.Factory {
    
    private final Context context;
    
    public ViewModelFactory(Context context) {
        this.context = context;
    }
    
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(context);
        } else if (modelClass.isAssignableFrom(UserViewModel.class)) {
            // 如果是Application级别的ViewModel，需要获取Application对象
            return (T) new UserViewModel((Application) context.getApplicationContext());
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
} 