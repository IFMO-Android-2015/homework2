package ru.ifmo.android_2015.citycam.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable{
    private Bitmap image;
    private String title;
    public Image(Bitmap image, String title) {
        this.image = image;
        this.title = title;
    }
    public Bitmap getImage() {
        return image;
    }
    public String getTitle() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeValue(image);
        parcel.writeString(title);
    }

    protected Image(Parcel parcel) {
        image = parcel.readParcelable(Bitmap.class.getClassLoader());
        title = parcel.readString();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

}
