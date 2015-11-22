package ru.ifmo.android_2015.citycam.webcams;

import android.graphics.Bitmap;

public class CamInfo {
    String camTitle;
    Double latitude, longitude;
    String previewUrl;
    Bitmap image;

    public void setCamTitle(String title) {
        this.camTitle = title;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setPreviewUrl(String url) {
        this.previewUrl = url;
    }

    public void setImage(Bitmap bitmap) {
        this.image = bitmap;
    }

    public String getCamTitle() {
        return camTitle;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public Bitmap getImage() {
        return image;
    }
}
