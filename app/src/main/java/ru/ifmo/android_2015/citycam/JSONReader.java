package ru.ifmo.android_2015.citycam;

/**
 * Created by Богдан on 27.12.2015.
 */

import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.WebCam;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

public class JSONReader {
    public static WebCam getCityData(final City city) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) Webcams.createNearbyUrl(city.latitude, city.longitude).openConnection();

        InputStream in = null;
        JsonReader reader = null;
        final WebCam data = new WebCam(null, null, null);

        try {
            final int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("JR", "Bad HTTP response");
            }

            in = connection.getInputStream();
            reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

            getContent(reader, data);

        } finally {
            if (reader != null) reader.close();
            if (in != null) in.close();
            connection.disconnect();
        }
        return data;
    }

    private static void getContent(final JsonReader reader, final WebCam data) throws IOException {
        reader.beginObject();
        while ((data.title == null || data.url == null) && reader.hasNext()) {
            final String token = reader.nextName();
            if (token.equals("title")) {
                data.title = reader.nextString();
            } else if (token.equals("preview_url")) {
                data.url = reader.nextString();
            } else if (token.equals("webcams")) {
                reader.beginObject();
            } else if (token.equals("webcam")) {
                reader.beginArray();
                reader.beginObject();
            } else {
                reader.skipValue();
            }
        }
    }
}