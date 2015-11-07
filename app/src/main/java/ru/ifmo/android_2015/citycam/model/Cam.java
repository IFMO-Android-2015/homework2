package ru.ifmo.android_2015.citycam.model;

/**
 * Created by Jackson on 07.11.2015.
 */
public class Cam {
    private boolean isActive;
    private String url;
    private float rating;

    public Cam(boolean isActive, String url, double rating) {
        this.isActive = isActive;
        this.url = url;
        this.rating = (float)rating;
    }

    public String getUrl() {
        return url;
    }

    public float getRating() {
        return rating;
    }

    public boolean isActive() {
        return isActive;
    }
}