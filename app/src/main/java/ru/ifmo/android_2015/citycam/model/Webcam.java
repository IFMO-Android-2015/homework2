package ru.ifmo.android_2015.citycam.model;

/**
 * Created by ruslanabdulhalikov on 02.11.15.
 */
public class Webcam {
    public final String title;
    public final String city;
    public final String imageUrl;
    public final double rating;

    public Webcam(String title, String city, String imageUrl, double rating) {
        this.title = title;
        this.city = city;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }
}
