package ru.ifmo.android_2015.citycam.webcams;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lalala on 26.12.15.
 */
public class Camera  implements Parcelable {
    public String title;
    public String preview_url;
    public Bitmap preview;
    public Double latitude;
    public Double longitude;

    public Camera () {}

    protected Camera(Parcel in) {
        title = in.readString();
        preview_url = in.readString();
        preview = in.readParcelable(Bitmap.class.getClassLoader());
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(preview_url);
        dest.writeValue(preview);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
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
    public int hashCode() {
        return title.hashCode();
    }
}
