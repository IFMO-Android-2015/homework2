package ru.ifmo.android_2015.citycam.webcams;

import java.util.Date;

public class CameraDescription {
    private String title;
    private CameraImage previewImage;
    private float averageRating;

    public CameraDescription(String title, CameraImage previewImage, float averageRating) {
        this.title = title;
        this.previewImage = previewImage;
        this.averageRating = averageRating;
    }

    public String getTitle() {
        return title;
    }

    public CameraImage getPreviewImage() {
        return previewImage;
    }

    public double getAverageRating() {
        return averageRating;
    }
}
