package com.healthx.model;

public class HealthCard {
    private int titleResId;
    private String summary;
    private int iconResId;

    public HealthCard(int titleResId, String summary, int iconResId) {
        this.titleResId = titleResId;
        this.summary = summary;
        this.iconResId = iconResId;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public String getSummary() {
        return summary;
    }

    public int getIconResId() {
        return iconResId;
    }
} 