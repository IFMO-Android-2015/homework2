package ru.ifmo.android_2015.citycam.util;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.JsonReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Created by alexey on 28.12.15.
 */
public class Loader {
    public static City.Data getCityData (final City city) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) Webcams.createNearbyUrl(city.latitude, city.longitude).openConnection();
        InputStream inputStream = null;
        City.Data cityData = new City.Data();
        try {
            checkResponseCode(connection.getResponseCode());
            inputStream = connection.getInputStream();
            JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            getInfo(jsonReader, cityData);
            jsonReader.close();
        } finally {
            if (inputStream != null) inputStream.close();
            connection.disconnect();
        }
        return cityData;
    }

    private static void getInfo (final JsonReader jsonReader, final City.Data cityData) throws IOException {
        jsonReader.beginObject();

        while ((cityData.title == null || cityData.url_of_preview == null) && jsonReader.hasNext()) {
            final String token = jsonReader.nextName();
            if (token.equals("title")) {
                cityData.title = jsonReader.nextString();
            } else if (token.equals("preview_url")) {
                cityData.url_of_preview = jsonReader.nextString();
            } else if (token.equals("webcams")) {
                jsonReader.beginObject();
            } else if (token.equals("webcam")) {
                jsonReader.beginArray();
                jsonReader.beginObject();
            } else {
                jsonReader.skipValue();
            }
        }
    }

    public static Bitmap getBitmap (final String url) throws IOException{
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        InputStream inputStream = null;
        Bitmap bitmap = null;
        try {
            checkResponseCode(connection.getResponseCode());
            inputStream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } finally {
            if (inputStream != null) inputStream.close();
            connection.disconnect();
        }
        return bitmap;
    }

    private static void checkResponseCode(final int responseCode) throws IOException {
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new FileNotFoundException("Bad HTTP response: " + responseCode);
        }
    }
}
