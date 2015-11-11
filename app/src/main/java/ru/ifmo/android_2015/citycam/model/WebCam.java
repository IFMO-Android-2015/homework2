package ru.ifmo.android_2015.citycam.model;

import java.net.URL;

public class WebCam {

    private String title;
    private String continent;
    private String country;
    private String city;
    private String url;
    private Double latitude;
    private Double longitude;

    public WebCam(String camTitle, String camContinent, String camCountry, String camCity, String camUrl, Double camLatitude, Double camLongitude) {
        title = camTitle;
        continent = camContinent;
        country = camCountry;
        city = camCity;
        url = camUrl;
        latitude = camLatitude;
        longitude = camLongitude;
    }

    public String getTitle() {
        return title;
    }

    public String getURL() {
        return url;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getLocation() {return continent + ", " + country + ", " + city; }
}
