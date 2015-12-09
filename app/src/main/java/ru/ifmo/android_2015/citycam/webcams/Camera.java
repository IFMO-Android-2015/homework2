package ru.ifmo.android_2015.citycam.webcams;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ilnar Sabirzyanov on 22.11.2015.
 */
public class Camera implements Parcelable{
    protected String title;
    protected String preview_url;
    protected Bitmap preview;
    protected Double latitude;
    protected Double longitude;
    protected Double rating;

    public Camera() {}

    protected Camera(Parcel in) {
        title = in.readString();
        preview_url = in.readString();
        preview = in.readParcelable(Bitmap.class.getClassLoader());
        latitude = in.readDouble();
        longitude = in.readDouble();
        rating = in.readDouble();
    }

    public static final Creator<Camera> CREATOR = new Creator<Camera>() {
        @Override
        public Camera createFromParcel(Parcel in) {
            return new Camera(in);
        }

        @Override
        public Camera[] newArray(int size) {
            return new Camera[size];
        }
    };

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

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPreview_url(String preview_url) {
        this.preview_url = preview_url;
    }

    public void setPreview(Bitmap preview) {
        this.preview = preview;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public String getPreview_url() {
        return preview_url;
    }

    public Bitmap getPreview() {
        return preview;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getRating() {
        return rating;
    }
}
