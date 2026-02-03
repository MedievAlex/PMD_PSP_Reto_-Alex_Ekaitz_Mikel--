package com.example.emastore;

public class MenuItem {
    private int id;
    private String title;
    private int imageResId;

    public MenuItem(int id, String title, int imageResId) {
        this.id = id;
        this.title = title;
        this.imageResId = imageResId;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }
}