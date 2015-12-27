package ru.ifmo.android_2015.citycam.webcams;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Denis on 26.12.2015.
 */
public class Webcam implements Parcelable {
    Double latitude;
    Double longitude;
    Double rating;
    String title;
    String previewUrl;
    Bitmap image;

    public Webcam() {};

    protected Webcam(Parcel source) {
        latitude = source.readDouble();
        longitude = source.readDouble();
        rating = source.readDouble();
        title = source.readString();
        previewUrl = source.readString();
        image = source.readParcelable(Bitmap.class.getClassLoader());
    }


    public static final Creator<Webcam> CREATOR = new Creator<Webcam>() {
        @Override
        public Webcam createFromParcel(Parcel source) {
            return new Webcam (source);
        }

        @Override
        public Webcam[] newArray(int size) {
            return new Webcam[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(rating);
        dest.writeString(title);
        dest.writeString(previewUrl);
        dest.writeValue(image);
    }

    @Override
    public int hashCode() {
        return title.hashCode();
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public void setImage(Bitmap image) {
        this.image = image;
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

    public String getTitle() {
        return title;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public Bitmap getImage() {
        return image;
    }







}
