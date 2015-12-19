package ru.ifmo.android_2015.citycam.model;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Webcam {
    public final String title;
    public final String image;
    public final double rating;

    public Webcam(String title, String image, double rating) {
        this.title = title;
        this.image = image;
        this.rating = rating;
    }
    public Bitmap downloadBitmapImage() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(image).openConnection();
        try {
            return BitmapFactory.decodeStream(conn.getInputStream());
        } finally {
            conn.disconnect();
        }
    }
}
