package com.example.emastore.model;

public class MediaItem {
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;

    private int type;
    private int resourceId;

    public MediaItem(int type, int resourceId) {
        this.type = type;
        this.resourceId = resourceId;
    }

    public int getType() { return type; }
    public int getResourceId() { return resourceId; }
}