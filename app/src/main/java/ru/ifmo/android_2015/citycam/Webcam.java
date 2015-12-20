package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;

/**
 * Created by 1 on 20.12.2015.
 */
public class Webcam {

    String title;
    double latitude;
    double longitude;
    Bitmap image;
    String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public static final Webcam NO_WEBCAM = new Webcam();
    public static final Webcam BAD_INTERNET = new Webcam();
}
