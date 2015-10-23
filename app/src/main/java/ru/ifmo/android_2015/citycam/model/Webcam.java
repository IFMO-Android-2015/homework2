package ru.ifmo.android_2015.citycam.model;

/**
 * Created by heat_wave on 10/23/15.
 */
public class Webcam {
    public final String title;
    public final String location;
    public final long unixTime;
    public final String imageUrl;

    public Webcam(String title, String location, long unixTime, String imageUrl) {
        this.title = title;
        this.location = location;
        this.unixTime = unixTime;
        this.imageUrl = imageUrl;
    }
}
