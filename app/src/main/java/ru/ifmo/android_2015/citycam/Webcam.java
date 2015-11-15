package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Created by ZeRoGerc
 * ITMO UNIVERSITY
 */

public class Webcam {
    private final String title;
    private final Date lastUpdate;
    private final URL previewURL;
    private Bitmap image = null;

    public Webcam() {
        this.title = null;
        this.lastUpdate = null;
        this.previewURL = null;
    }

    public Webcam(String title, Long lastUpdate, String previewURL) throws MalformedURLException {
        this.title = title;
        this.lastUpdate = new Date(lastUpdate * 1000);
        this.previewURL = new URL(previewURL);
    }

    public void attachImage(Bitmap bitmap) {
        this.image = bitmap;
    }

    public String getTitle() {
        return title;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public URL getPreviewURL() {
        return previewURL;
    }

    public Bitmap getImage() {
        return image;
    }
}
