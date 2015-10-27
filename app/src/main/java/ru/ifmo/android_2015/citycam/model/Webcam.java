package ru.ifmo.android_2015.citycam.model;

import java.net.URL;

/**
 * Created by ruslanthakohov on 26/10/15.
 */

public class Webcam {

    /**
     * Title of the webcam
     */
    public final String title;

    /**
     * Number of views since this webcam has been added
     */
    public final long viewCount;

    /**
     * Name of the city the webcam is located at
     */
    public final String city;

    /**
     * Average rating for this webcam. Values from 0 to 5.
     */
    public final double ratingAvg;

    /**
     * URL to a preview image for this webcam. JPG, 360 pixel wide, variable height.
     */
    public final URL previewURL;

    public Webcam(String title, long viewCount, String city, double ratingAvg, URL previewURL) {
        this.title = title;
        this.viewCount = viewCount;
        this.city = city;
        this.ratingAvg = ratingAvg;
        this.previewURL = previewURL;
    }

    @Override
    public String toString() {
        return "title: " + title + "; city: " + city;
    }

}
