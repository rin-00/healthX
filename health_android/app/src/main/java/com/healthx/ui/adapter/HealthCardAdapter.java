package com.healthx.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.healthx.R;
import com.healthx.model.HealthCard;

import java.util.List;

public class HealthCardAdapter extends RecyclerView.Adapter<HealthCardAdapter.HealthCardViewHolder> {

    private final List<HealthCard> healthCards;

    public HealthCardAdapter(List<HealthCard> healthCards) {
        this.healthCards = healthCards;
    }

    @NonNull
    @Override
    public HealthCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health_card, parent, false);
        return new HealthCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HealthCardViewHolder holder, int position) {
        HealthCard card = healthCards.get(position);
        holder.bind(card);
    }

    @Override
    public int getItemCount() {
        return healthCards.size();
    }

    static class HealthCardViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView summaryView;
        private final Button actionButton;

        public HealthCardViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.image_card_icon);
            titleView = itemView.findViewById(R.id.text_card_title);
            summaryView = itemView.findViewById(R.id.text_card_summary);
            actionButton = itemView.findViewById(R.id.button_card_action);
        }

        public void bind(HealthCard card) {
            iconView.setImageResource(card.getIconResId());
            titleView.setText(card.getTitleResId());
            summaryView.setText(card.getSummary());
            
            actionButton.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), 
                    itemView.getContext().getString(card.getTitleResId()), 
                    Toast.LENGTH_SHORT).show();
            });
            
            itemView.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), 
                    itemView.getContext().getString(card.getTitleResId()) + " 详情", 
                    Toast.LENGTH_SHORT).show();
            });
        }
    }
} 