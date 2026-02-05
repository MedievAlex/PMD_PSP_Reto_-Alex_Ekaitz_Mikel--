package com.example.emastore.model;

public class MenuItem {
    private int id;
    private String title;
    private int iconResId;

    public MenuItem(int id, String title, int iconResId) {
        this.id = id;
        this.title = title;
        this.iconResId = iconResId;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getIconResId() { return iconResId; }
}