package ru.ifmo.android_2015.citycam.model;

import android.graphics.Bitmap;

/**
 * Created by ruslandavletshin on 02/11/15.
 */
public class CityData {

    public String title, preview_url;
    public Bitmap preview_image;

    public CityData (final String title, final String preview_url, final Bitmap preview_image) {
        this.title = title;
        this.preview_url = preview_url;
        this.preview_image = preview_image;
    }
}
