package com.example.emastore.model;

import android.graphics.Bitmap;

public class MenuItem {
    private String title;
    private Bitmap icon;

    public MenuItem(String title, Bitmap icon) {
        this.title = title;
        this.icon = icon;
    }

    // Getters
    public String getTitle() { return title; }
    public Bitmap getIcon() { return icon; }
}
