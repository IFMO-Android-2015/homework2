package ru.ifmo.android_2015.citycam.webcams;

import android.graphics.Bitmap;

/**
 * Created by alice on 25.12.15.
 */
public class MyCamera {
    public String preview_url;
    public String user;
    public String user_url;
    public String title;
    public String cityName;

    public Bitmap cameraImage;

    public MyCamera (String preview_url, String user, String user_url, String title) {
        this.preview_url = preview_url;
        this.user = user;
        this.user_url = user_url;
        this.title = title;
    }

    public void setCameraImage(Bitmap bitmap) {
        this.cameraImage = bitmap;
    }
    public void setCityName(String name) {
        this.cityName = name;
    }

}
