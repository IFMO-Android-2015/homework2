package ru.ifmo.android_2015.citycam.webcams;

import android.graphics.Bitmap;

/**
 * Created by baba_beda on 11/1/15.
 */
public class WebcamData {
    private Bitmap bitmap;
    private String user;
    private String title;
    private long time;

    public WebcamData() {
        user = "";
        title = "";
        time = 0L;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
