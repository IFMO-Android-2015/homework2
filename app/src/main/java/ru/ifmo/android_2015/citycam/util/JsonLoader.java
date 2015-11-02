package ru.ifmo.android_2015.citycam.util;

import android.util.JsonReader;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.CityData;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Created by ruslandavletshin on 02/11/15.
 */
public class JsonLoader {
    public static CityData getCityData (final City city) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) Webcams.createNearbyUrl(city.latitude, city.longitude).openConnection();

        InputStream in = null;
        JsonReader reader = null;
        final CityData data = new CityData(null, null, null);

        try {
            final int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new FileNotFoundException("Bad HTTP response: " + responseCode);
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

    private static void getContent (final JsonReader reader, final CityData data) throws IOException {
//        reader.beginArray();
        reader.beginObject();
        while ((data.title == null || data.preview_url == null) && reader.hasNext()) {
            final String token = reader.nextName();
            if (token.equals("title")) {
                data.title = reader.nextString();
            } else if (token.equals("preview_url")) {
                data.preview_url = reader.nextString();
            } else if (token.equals("webcams")) {
                reader.beginObject();
            } else if (token.equals("webcam")) {
                reader.beginArray();
                reader.beginObject();
            } else {
                reader.skipValue();
            }
        }
//        reader.endArray();
    }
}
