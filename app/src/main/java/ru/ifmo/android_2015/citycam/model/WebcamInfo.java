package ru.ifmo.android_2015.citycam.model;

import java.net.URL;

/**
 * Created by sofya on 14.11.15.
 */
public class WebcamInfo {
    public String cameraUrl;
    public String name;
    public String city;
    public String country;

    public WebcamInfo() {

    }

    public WebcamInfo(String url, String name, String city, String country) {
        this.cameraUrl = url;
        this.name = name;
        this.city = city;
        this.country = country;
    }
}
