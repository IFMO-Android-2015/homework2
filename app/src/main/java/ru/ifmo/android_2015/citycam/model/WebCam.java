package ru.ifmo.android_2015.citycam.model;

/**
 * Created by Богдан on 27.12.2015.
 */

import android.graphics.Bitmap;

public class WebCam {

    public String title, url;
    public Bitmap image;

    public WebCam(String inTitle, String inUrl, Bitmap inImage) {
        this.title = inTitle;
        this.url = inUrl;
        this.image = inImage;
    }
}