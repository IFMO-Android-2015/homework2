package ru.ifmo.android_2015.citycam;

import android.os.Parcel;
import android.os.Parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Dan) on 08.11.2015.
 */
public class Cam implements Parcelable {

    public String title;
    public String preview_url;

    public Cam() {
        this.title = null;
        this.preview_url = null;

    }
    public Cam(String title, String preview_url) {

        this.title = title;
        this.preview_url = preview_url;

    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(title);
        parcel.writeString(preview_url);
    }

    public static final Parcelable.Creator<Cam> CREATOR = new Parcelable.Creator<Cam>() {
        // распаковываем объект из Parcel
        public Cam createFromParcel(Parcel in) {
            return new Cam(in);
        }

        public Cam[] newArray(int size) {
            return new Cam[size];
        }
    };

    // конструктор, считывающий данные из Parcel
    private Cam(Parcel parcel) {
        title = parcel.readString();
        preview_url = parcel.readString();
    }
}