package ru.ifmo.android_2015.citycam.webcams;

import android.graphics.Bitmap;

public class Webcam {
    Double latitude, longitude;
    String previewUrl, title;
    Bitmap image;

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }


    public String getPreviewUrl() {
        return previewUrl;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getTitle() {
        return title;
    }

    public Bitmap getImage() {
        return image;
    }

}
