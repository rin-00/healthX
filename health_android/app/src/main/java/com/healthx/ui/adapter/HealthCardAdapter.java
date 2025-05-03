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
    private OnItemClickListener listener;

    public HealthCardAdapter(List<HealthCard> healthCards) {
        this.healthCards = healthCards;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
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
        holder.bind(card, listener);
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

        public void bind(HealthCard card, final OnItemClickListener listener) {
            iconView.setImageResource(card.getIconResId());
            titleView.setText(card.getTitleResId());
            summaryView.setText(card.getSummary());
            
            actionButton.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), 
                    itemView.getContext().getString(card.getTitleResId()), 
                    Toast.LENGTH_SHORT).show();
            });
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(card);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(HealthCard healthCard);
    }
} 