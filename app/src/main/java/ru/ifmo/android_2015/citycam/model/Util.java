package ru.ifmo.android_2015.citycam.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    // Closing objects without throwing errors

    private static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                Log.e("UTIL", "Failed to close resource: " + e.getMessage());
            }
        }
    }

    public static Bitmap downloadBitmap(String url) throws IOException {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            in = conn.getInputStream();
            return BitmapFactory.decodeStream(in);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            closeQuietly(in);
        }
    }

    public static WebcamInfo parseRespond(HttpURLConnection connection) throws IOException {
        InputStream stream = null;
        InputStreamReader inputReader = null;
        JsonReader json = null;
        try {
            stream = connection.getInputStream();
            inputReader = new InputStreamReader(stream);
            json = new JsonReader(inputReader);

            json.beginObject();

            int view_count = -1;
            double rating = -1;
            String country = null, city = null, url = null;

            while (json.hasNext()) {
                if (json.peek() == JsonToken.BEGIN_OBJECT) {
                    json.beginObject();
                }

                String name = json.nextName();
                switch (name) {
                    case "status":
                        String status = json.nextString();
                        if (!status.equals("ok")) {
                            throw new IOException("Request failed");
                        }
                        break;
                    case "webcams":
                        json.beginObject();
                        break;
                    case "count":
                        int count = json.nextInt();
                        if (count == 0) {
                            throw new WebcamNotFoundException();
                        }
                        break;
                    case "webcam":
                        json.beginArray();
                        break;
                    case "view_count":
                        view_count = json.nextInt();
                        break;
                    case "country":
                        country = json.nextString();
                        break;
                    case "city":
                        city = json.nextString();
                        break;
                    case "rating_avg":
                        rating = json.nextDouble();
                        break;
                    case "preview_url":
                        url = json.nextString();
                        break;
                    default:
                        json.skipValue();
                        break;
                }
                if (json.peek() == JsonToken.END_OBJECT) {
                    return new WebcamInfo(url, country, city, view_count, rating);
                }
            }
        } finally {
            closeQuietly(json);
            closeQuietly(inputReader);
            closeQuietly(stream);
        }
        return null;
    }
}