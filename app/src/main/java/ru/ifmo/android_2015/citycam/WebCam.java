package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;

public class WebCam {

    private Bitmap bitmap;
    private String title;
    private double rate;

    public WebCam(Bitmap bitmap, double rate, String title) {
        this.bitmap = bitmap;
        this.rate = rate;
        this.title = title;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getTitle() {
        return title;
    }

    public double getRate() {
        return rate;
    }
}

