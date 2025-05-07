package com.healthx.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.healthx.R;
import com.healthx.model.Exercise;
import com.healthx.util.DateTimeUtils;

import org.threeten.bp.format.DateTimeFormatter;

public class ExerciseAdapter extends ListAdapter<Exercise, ExerciseAdapter.ExerciseViewHolder> {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeUtils.UI_DATETIME_FORMAT;
    private OnExerciseClickListener listener;
    
    public ExerciseAdapter() {
        super(DIFF_CALLBACK);
    }
    
    private static final DiffUtil.ItemCallback<Exercise> DIFF_CALLBACK = new DiffUtil.ItemCallback<Exercise>() {
        @Override
        public boolean areItemsTheSame(@NonNull Exercise oldItem, @NonNull Exercise newItem) {
            return oldItem.getId() == newItem.getId();
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull Exercise oldItem, @NonNull Exercise newItem) {
            return oldItem.getExerciseName().equals(newItem.getExerciseName()) &&
                    oldItem.getDuration() == newItem.getDuration() &&
                    oldItem.getCaloriesBurned() == newItem.getCaloriesBurned() &&
                    oldItem.getExercisedAt().equals(newItem.getExercisedAt());
        }
    };
    
    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise currentExercise = getItem(position);
        holder.bind(currentExercise);
    }
    
    public Exercise getExerciseAt(int position) {
        return getItem(position);
    }
    
    public void setOnExerciseClickListener(OnExerciseClickListener listener) {
        this.listener = listener;
    }
    
    public class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvExerciseName;
        private TextView tvExerciseType;
        private TextView tvExerciseIntensity;
        private TextView tvExerciseDate;
        private TextView tvExerciseTime;
        private TextView tvExerciseCalories;
        
        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tv_exercise_name);
            tvExerciseType = itemView.findViewById(R.id.tv_exercise_type);
            tvExerciseIntensity = itemView.findViewById(R.id.tv_exercise_intensity);
            tvExerciseDate = itemView.findViewById(R.id.tv_exercise_date);
            tvExerciseTime = itemView.findViewById(R.id.tv_exercise_time);
            tvExerciseCalories = itemView.findViewById(R.id.tv_exercise_calories);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onExerciseClick(getItem(position));
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onExerciseLongClick(getItem(position));
                    return true;
                }
                return false;
            });
        }
        
        public void bind(Exercise exercise) {
            tvExerciseName.setText(exercise.getExerciseName());
            
            String exerciseType = exercise.getExerciseType();
            if (exerciseType != null && !exerciseType.isEmpty()) {
                tvExerciseType.setText(exerciseType);
                tvExerciseType.setVisibility(View.VISIBLE);
            } else {
                tvExerciseType.setVisibility(View.GONE);
            }
            
            String intensity = exercise.getIntensity();
            if (intensity != null && !intensity.isEmpty()) {
                tvExerciseIntensity.setText(intensity);
                tvExerciseIntensity.setVisibility(View.VISIBLE);
            } else {
                tvExerciseIntensity.setVisibility(View.GONE);
            }
            
            tvExerciseDate.setText(exercise.getExercisedAt().format(DATE_TIME_FORMATTER));
            tvExerciseTime.setText(exercise.getDuration() + " 分钟");
            tvExerciseCalories.setText(String.format("%.0f 千卡", exercise.getCaloriesBurned()));
        }
    }
    
    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
        void onExerciseLongClick(Exercise exercise);
    }
} 