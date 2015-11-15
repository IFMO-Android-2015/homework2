package ru.ifmo.android_2015.citycam.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Камера
 */
public class Webcam implements Parcelable {

    /**
     * Название камеры
     */
    public final String title;
    /**
     * Время последнего обновления
     */
    public final Date lastUpdate;
    /**
     * Картинка превью
     */
    public Bitmap preview = null;
    /**
     * Ссылка на превью
     */
    public final URL previewURL;


    public Webcam(String title, int timestamp, URL previewURL) {
        this.title = title;
        this.lastUpdate = new Date(timestamp * 1000L);
        this.previewURL = previewURL;
    }

    public boolean updatePreview() {
        try {
            this.preview = downloadPreview(previewURL);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Webcam[title=\"" + title + "\" lastUpdate=" + lastUpdate.toString() + "]";
    }

    private Bitmap downloadPreview(URL previewURL) throws IOException {
        HttpURLConnection imageDlConnection = (HttpURLConnection) previewURL.openConnection();
        InputStream imageStream = imageDlConnection.getInputStream();

        Bitmap newView = BitmapFactory.decodeStream(imageStream);

        imageDlConnection.disconnect();

        return newView;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeSerializable(lastUpdate);
        dest.writeSerializable(previewURL);
        dest.writeValue(preview);
    }

    protected Webcam(Parcel src) {
        title = src.readString();
        lastUpdate = (Date) src.readSerializable();
        previewURL = (URL) src.readSerializable();
        preview = src.readParcelable(Bitmap.class.getClassLoader());
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
