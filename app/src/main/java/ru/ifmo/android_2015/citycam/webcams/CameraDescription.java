package ru.ifmo.android_2015.citycam.webcams;

/**
 * Created by annafrolova on 27.12.15.
 */
public class CameraDescription {
    private String name;
    private CameraImage previewImage;
    private double rating;

    public CameraDescription(String name,CameraImage previewImage,double rating)
    {
        this.name=name;
        this.previewImage=previewImage;
        this.rating=rating;
    }
    public String getName()
    {
        return name;
    }
    public CameraImage getPreviewImage()
    {
        return previewImage;
    }
    public double getRating()
    {
        return rating;
    }
}