package ru.ifmo.android_2015.citycam.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StrictMode;

/**
 * Город
 */
public class City implements Parcelable {

    /**
     * Название
     */
    public final String name;

    /**
     * Широта
     */
    public final double latitude;

    /**
     * Долгота
     */
    public final double longitude;


    public City(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "City[name=\"" + name + "\" lat=" + latitude + " lon=" + longitude + "]";
    }


    // --------- Методы интерфейса Parcelable ------------
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    protected City(Parcel src) {
        name = src.readString();
        latitude = src.readDouble();
        longitude = src.readDouble();
    }

    public static final Creator<City> CREATOR = new Creator<City>() {
        @Override
        public City createFromParcel(Parcel source) {
            return new City(source);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };

    public static class Data {
        public String title, url_of_preview;
        public Bitmap image;

        public Data (final String title, final String url_of_preview, final Bitmap image) {
            this.title = title;
            this.url_of_preview = url_of_preview;
            this.image = image;
        }

        public Data() {
            this.title = null;
            this.url_of_preview = null;
            this.image = null;
        }
    }
}
