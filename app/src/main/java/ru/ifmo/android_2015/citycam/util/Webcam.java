package ru.ifmo.android_2015.citycam.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Webcam {
    private String previewUrl;
    private Bitmap previewImage;

    public Webcam(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public void downloadPhoto() throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(previewUrl).openConnection();
            previewImage = BitmapFactory.decodeStream(connection.getInputStream());
        } finally {
            if (connection == null) {
                connection.disconnect();
            }
        }
    }

    public Bitmap getPreviewImage() {
        return previewImage;
    }

    @Override
    public String toString() {
        return "Webcam{" +
                "previewUrl='" + previewUrl + '\'' +
                '}';
    }
}
