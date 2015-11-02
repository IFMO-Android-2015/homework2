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
        InputStream input;
        StringBuilder total;
        try {
            conn = (HttpURLConnection) Webcams.createNearbyUrl(city.latitude, city.longitude).openConnection();
            responseCode = conn.getResponseCode();
            input = conn.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(input));
            total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            mydata = total.toString();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Wrong response code");
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        value = parse(total.toString(), params, paramsNumber);
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

    String[] parse(String in, String[] params, int num) throws IOException, JSONException {
        JSONObject parsedJSON = null;
        parsedJSON = new JSONObject(in);
        if (!parsedJSON.getString("status").equals("ok")) {
            throw new IOException("Request Failed");
        }
        parsedJSON = parsedJSON.getJSONObject("webcams");
        int s = parsedJSON.getInt("count");
        String[] ans = null;
        if (s != 0) {
            parsedJSON = (JSONObject) parsedJSON.getJSONArray("webcam").get(0);
            ans = new String[num];
            for (int i = 0; i < num; i++) {
                ans[i] = parsedJSON.getString(params[i]);
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
