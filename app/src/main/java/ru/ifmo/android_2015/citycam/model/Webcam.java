package ru.ifmo.android_2015.citycam.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Webcam implements Parcelable {
    private Bitmap preview;
    private String title;
    private double rating;
    private double timeOffset;
    private String timezone;

    public Webcam(Bitmap preview, String title, double rating, String timezone, double timeOffset) {
        this.preview = preview;
        this.title = title;
        this.rating = rating;
        this.timeOffset = timeOffset;
        this.timezone = timezone;
    }

    public Bitmap getPreview() {
        return preview;
    }

    public String getTitle() {
        return title;
    }

    public String getTimezone() {
        return timezone;
    }

    public double getRating() {
        return rating;
    }

    public double getTimeOffset() {
        return timeOffset;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(preview);
        dest.writeString(title);
        dest.writeSerializable(rating);
        dest.writeString(timezone);
        dest.writeSerializable(timeOffset);
    }

    protected Webcam(Parcel src) {
        preview = src.readParcelable(Bitmap.class.getClassLoader());
        title = src.readString();
        rating = src.readDouble();
        timezone = src.readString();
        timeOffset = src.readDouble();
    }

    public static final Creator<Webcam> CREATOR = new Creator<Webcam>() {
        @Override
        public Webcam createFromParcel(Parcel source) {
            return new Webcam(source);
        }

        @Override
        public Webcam[] newArray(int size) {
            return new Webcam[size];
        }
    };

}


