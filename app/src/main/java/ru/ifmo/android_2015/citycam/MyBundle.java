package ru.ifmo.android_2015.citycam;


import android.graphics.Bitmap;

public class MyBundle {
    Bitmap img;
    PictureLoader task;
    String title;

    MyBundle(PictureLoader task, Bitmap img, String message) {
        this.task = task;
        this.img = img;
        this.title = message;
    }
}
