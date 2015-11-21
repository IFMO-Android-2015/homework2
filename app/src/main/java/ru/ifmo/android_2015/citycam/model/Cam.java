package ru.ifmo.android_2015.citycam.model;
/**
 * Created by Лиза on 18.11.2015.
 */

public class Cam {
    private String name = "";
    private String preview_url = "";

    public Cam(String name, String preview_url) {
        this.name = name;
        this.preview_url = preview_url;
    }

    public String getName() {
        return name;
    }

     public String getPreview_url() {
         return preview_url;
     }
}
