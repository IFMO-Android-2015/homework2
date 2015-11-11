package ru.ifmo.android_2015.citycam.model;

import ru.ifmo.android_2015.citycam.CityCamActivity;

/**
 * Created by andrey on 10.11.15.
 */
public class Webcam {
    private String name = "";
    private String preview_url = "";

    public Webcam(String name, String preview_url) {
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

