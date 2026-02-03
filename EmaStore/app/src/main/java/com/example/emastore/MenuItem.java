package com.example.emastore;

public class MenuItem {
    private int id;
    private String title;
    private int iconResource;

    public MenuItem(int id, String title, int iconResource) {
        this.id = id;
        this.title = title;
        this.iconResource = iconResource;
    }

    // Getters y setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getIconResource() { return iconResource; }
}