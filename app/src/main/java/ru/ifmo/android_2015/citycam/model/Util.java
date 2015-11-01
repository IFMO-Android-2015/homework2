package ru.ifmo.android_2015.citycam.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;

import java.io.IOException;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Util {

    public static class WebcamInfo implements Parcelable {
        public String pictureUrl, country, city;
        public int viewCount;
        public double rating;

        public WebcamInfo(String pictureUrl, String country,
                          String city, int viewCount, double rating) {
            this.pictureUrl = pictureUrl;
            this.country = country;
            this.city = city;
            this.viewCount = viewCount;
            this.rating = rating;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(country);
            dest.writeString(city);
            dest.writeInt(viewCount);
            dest.writeDouble(rating);
        }

        public WebcamInfo(Parcel p) {
            pictureUrl = "";
            country = p.readString();
            city = p.readString();
            viewCount = p.readInt();
            rating = p.readDouble();
        }

        public static final Creator<WebcamInfo> CREATOR = new Creator<WebcamInfo>() {
            @Override
            public WebcamInfo createFromParcel(Parcel source) {
                return new WebcamInfo(source);
            }

            @Override
            public WebcamInfo[] newArray(int size) {
                return new WebcamInfo[size];
            }
        };
    }


    public static Bitmap downloadBitmap(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        return BitmapFactory.decodeStream(conn.getInputStream());
    }

    private static void skip(JsonReader js, int k) throws IOException {
        for (int i = 0; i < k; i++) {
            js.skipValue();
            js.skipValue();
        }
    }

    public static WebcamInfo parseRespond(Reader in) throws IOException {
        JsonReader json = new JsonReader(in);
        json.beginObject();
        json.nextName();
        String status = json.nextString();
        if (!status.equals("ok")) {
            throw new IOException("Request failed");
        }
        json.skipValue();
        json.beginObject();

        json.skipValue();
        if (json.nextInt() == 0) {
            throw new WebcamNotFoundException();
        }

        skip(json, 2);

        json.skipValue();
        json.beginArray();
        json.beginObject();
        skip(json, 5);

        json.skipValue();
        int view_count = json.nextInt();

        skip(json, 8);
        json.skipValue();
        String country = json.nextString();

        json.skipValue();
        String city = json.nextString();

        skip(json, 1);

        json.skipValue();
        double rating = json.nextDouble();

        skip(json, 10);

        json.skipValue();
        String url = json.nextString();

        json.close();
        return new WebcamInfo(url, country, city, view_count, rating);
    }
}