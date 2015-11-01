package ru.ifmo.android_2015.citycam.webcams;

import android.graphics.Bitmap;

import java.net.URL;

/**
 * Created by kurkin on 01.11.15.
 */
public class Webcam {
    URL preview_url;
    Double latitude;
    Double longitude;
    Bitmap image;
    String title;

    public Webcam() {
    }

    public void setPreview_url(URL preview_url) {
        this.preview_url = preview_url;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public URL getPreview_url() {
        return preview_url;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }
}
