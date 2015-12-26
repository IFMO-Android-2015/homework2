package ru.ifmo.android_2015.citycam.model;

import java.net.URL;
import java.util.Date;

/**
 * Created by dns on 26.12.2015.
 */
public class WebCam {
    private final URL url;
    private final String title;
    private final boolean exists;
    private final Date lastUpdate;
    private final double rating;

    public WebCam(URL url, String title, Date lastUpdate, double rating) {
        this.url = url;
        this.title = title;
        this.lastUpdate = lastUpdate;
        this.rating = rating;
        this.exists = true;
    }

    public WebCam() {
        this.url = null;
        this.title = null;
        this.lastUpdate = new Date();
        this.rating = 0.0;
        this.exists = false;
    }

    public URL getURL() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public boolean exists() {
        return exists;
    }

    public double getRating() {
        return rating;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }
}

