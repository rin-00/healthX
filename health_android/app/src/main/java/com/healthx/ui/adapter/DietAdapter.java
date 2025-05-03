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
import com.healthx.model.Diet;

import org.threeten.bp.format.DateTimeFormatter;

public class DietAdapter extends ListAdapter<Diet, DietAdapter.DietViewHolder> {
    
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private OnDietClickListener listener;
    
    public DietAdapter() {
        super(DIFF_CALLBACK);
    }
    
    public void setOnDietClickListener(OnDietClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public DietViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diet, parent, false);
        return new DietViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DietViewHolder holder, int position) {
        Diet diet = getItem(position);
        holder.bind(diet);
    }
    
    class DietViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFoodName;
        private TextView tvCalories;
        private TextView tvMealType;
        private TextView tvEatenTime;
        private TextView tvNutrition;
        
        public DietViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tv_food_name);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvMealType = itemView.findViewById(R.id.tv_meal_type);
            tvEatenTime = itemView.findViewById(R.id.tv_eaten_time);
            tvNutrition = itemView.findViewById(R.id.tv_nutrition);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDietClick(getItem(position));
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDietLongClick(getItem(position));
                    return true;
                }
                return false;
            });
        }
        
        public void bind(Diet diet) {
            tvFoodName.setText(diet.getFoodName());
            tvCalories.setText(String.format("%.0f 千卡", diet.getCalories()));
            
            String mealTypeText;
            switch (diet.getMealType()) {
                case "BREAKFAST":
                    mealTypeText = "早餐";
                    break;
                case "LUNCH":
                    mealTypeText = "午餐";
                    break;
                case "DINNER":
                    mealTypeText = "晚餐";
                    break;
                case "SNACK":
                    mealTypeText = "零食";
                    break;
                default:
                    mealTypeText = diet.getMealType();
            }
            tvMealType.setText(mealTypeText);
            
            String timeText = diet.getEatenAt().format(timeFormatter);
            tvEatenTime.setText(timeText);
            
            String nutritionText = String.format("蛋白质: %.1fg  碳水: %.1fg  脂肪: %.1fg", 
                    diet.getProtein(), diet.getCarbs(), diet.getFat());
            tvNutrition.setText(nutritionText);
        }
    }
    
    private static final DiffUtil.ItemCallback<Diet> DIFF_CALLBACK = new DiffUtil.ItemCallback<Diet>() {
        @Override
        public boolean areItemsTheSame(@NonNull Diet oldItem, @NonNull Diet newItem) {
            return oldItem.getId() == newItem.getId();
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull Diet oldItem, @NonNull Diet newItem) {
            return oldItem.getFoodName().equals(newItem.getFoodName()) &&
                   oldItem.getCalories() == newItem.getCalories() &&
                   oldItem.getMealType().equals(newItem.getMealType()) &&
                   oldItem.getEatenAt().equals(newItem.getEatenAt());
        }
    };
    
    public interface OnDietClickListener {
        void onDietClick(Diet diet);
        void onDietLongClick(Diet diet);
    }
}