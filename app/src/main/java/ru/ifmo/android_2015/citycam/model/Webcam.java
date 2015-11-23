package ru.ifmo.android_2015.citycam.model;

import android.graphics.Bitmap;

public class Webcam {
    public Exception exception;
    public String ImageUrl;
    public String title;
    public int viewCount;
    public Bitmap ImageBitmap;

    public Webcam() {

    }

    public Webcam(Exception exception) {
        this.exception = exception;
    }
}
