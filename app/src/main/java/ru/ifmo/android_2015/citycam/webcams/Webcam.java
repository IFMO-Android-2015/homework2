package ru.ifmo.android_2015.citycam.webcams;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;


public class Webcam implements Parcelable {
    private String title;
    private String preview_url;
    private Bitmap preview;
    private Double latitude;
    private Double longitude;
    private Double rating;

    public Webcam() {}

    protected Webcam(Parcel source) {
        title = source.readString();
        preview_url = source.readString();
        preview = source.readParcelable(Bitmap.class.getClassLoader());
        latitude = source.readDouble();
        longitude = source.readDouble();
        rating = source.readDouble();
    }

    public void setTitle(String title1) {
        title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setPreview_url(String preview_url1) {
        preview_url = preview_url1;
    }

    public String getPreview_url() {
        return preview_url;
    }

    public void setPreview(Bitmap preview1) {
        preview = preview1;
    }

    public Bitmap getPreview() {
        return preview;
    }

    public void setLatitude(Double latitude1) {
        latitude = latitude1;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLongitude(Double longitude1) {
        longitude = longitude1;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setRating(Double rating1) {
        rating = rating1;
    }

    public Double getRating() {
        return rating;
    }

    // --------- Методы интерфейса Parcelable ------------
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(preview_url);
        dest.writeValue(preview);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(rating);
    }

    public static final Creator<Webcam> CREATOR = new Creator<Webcam>() {
        @Override
        public Webcam createFromParcel(Parcel in) {
            return new Webcam(in);
        }

        @Override
        public Webcam[] newArray(int size) {
            return new Webcam[size];
        }
    };

}
