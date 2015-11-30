package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Created by Cawa on 01.11.2015.
 */
public class Webcam implements Parcelable {
    City city;
    String mydata;
    Bitmap picture;
    String[] value;
    String[] params = {"country", "view_count", "rating_avg", "preview_url"};
    int paramsNumber = 4;

    Webcam(City cityIn) {
        value = new String[paramsNumber];
        city = cityIn;
    }

    void getInfo() throws IOException, JSONException{
        mydata = params[3];
        HttpURLConnection conn = null;
        int responseCode = 0;
        JsonReader reader;
        try {
            conn = (HttpURLConnection) Webcams.createNearbyUrl(city.latitude, city.longitude).openConnection();
            responseCode = conn.getResponseCode();
            reader = new JsonReader(new InputStreamReader(conn.getInputStream()));
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Wrong response code");
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        value = parse(reader, params, paramsNumber);
        if (value != null) {
            try {
                picture = downloadBitmap(value[3]);
            } catch (Exception e) {
                throw new IOException("Wrong picture's URL");
            }
        }
    }

    public static Bitmap downloadBitmap(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        return BitmapFactory.decodeStream(conn.getInputStream());
    }

    String[] parse(JsonReader reader, String[] params, int num) throws IOException, JSONException {
        reader.beginObject();
        String ans[] = null;
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "status":
                    if (!reader.nextString().equals("ok")) {
                        throw new IOException("Request Failed");
                    }
                    break;
                case "count":
                    if (reader.nextInt() == 0) {
                        return null;
                    }
                case "webcams":
                    ans = new String[num];
                    reader.beginObject();
                    while (!reader.nextName().equals("webcam")) {
                        reader.skipValue();
                    }
                    reader.beginArray();
                    if (!reader.hasNext()) {
                        return null;
                    }
                    reader.beginObject();
                    while (reader.hasNext()) {
                        boolean t = true;

                        String name = reader.nextName();
                        for (int i = 0; i < num; i++) {
                            if (params[i].equals(name)) {
                                ans[i] = reader.nextString();
                                t = false;
                                break;
                            }
                        }
                        if (t) {
                            reader.skipValue();
                        }
                    }
                    return ans;
                default:
                    reader.skipValue();
            }
        }
        return ans;
    }

    // --------- Методы интерфейса Parcelable ------------
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(city.name);
        dest.writeDouble(city.latitude);
        dest.writeDouble(city.longitude);
        for (int i = 0; i < paramsNumber; i++) {
            dest.writeString(params[i]);
        }
    }

    protected Webcam(Parcel src) {
        city = new City(src.readString(), src.readDouble(),src.readDouble());
        for (int i = 0; i < paramsNumber; i++) {
            params[i]= src.readString();
        }
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
