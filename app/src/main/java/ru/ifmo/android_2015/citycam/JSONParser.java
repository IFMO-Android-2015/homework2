package ru.ifmo.android_2015.citycam;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ru.ifmo.android_2015.citycam.model.WebCam;

public class JSONParser {

    private static final String TAG = "JSONParser";

    public static List readJsonStream(InputStream in) throws IOException {

        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

        reader.beginObject();
        reader.nextName();
        String status = reader.nextString();
        if (!status.equals("ok")) {
            Log.e(TAG, "Status isn't Ok");
        }

        try {
            if (reader.nextName().equals("webcams")) {
                reader.beginObject();
                reader.skipValue(); //skip "count"
            }

            if (reader.nextInt() == 0) {
                Log.e(TAG, "No webcam in selected city");
                return null;
            }

            for (int i = 0; i < 5; i++) {
                reader.skipValue();
            }

            List<WebCam> webcams = new ArrayList<WebCam>();
            reader.beginArray();
            while (reader.hasNext()) {
                webcams.add(readWebcam(reader));
            }
            reader.endArray();
            return webcams;
        }  catch (Exception e) {
            Log.e(TAG, "Error in readJsonStream");
            return null;
        } finally {
            reader.close();
        }
    }

    static WebCam readWebcam(JsonReader reader) throws IOException {
        String title = null;
        String continent = null;
        String country = null;
        String city = null;
        String url= null;
        Double latitude = -1.0;
        Double longitude = -1.0;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("title")) {
                title = reader.nextString();
            } else if (name.equals("continent")) {
                continent = reader.nextString();
            } else if (name.equals("country")) {
                country = reader.nextString();
            } else if (name.equals("city")) {
                city = reader.nextString();
            } else if (name.equals("preview_url")) {
                url = reader.nextString();
            } else if (name.equals("latitude")) {
                latitude = reader.nextDouble();
            } else if (name.equals("longitude")) {
                longitude = reader.nextDouble();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new WebCam(title, continent, country, city, url, latitude, longitude);
    }

    private JSONParser() {}
}
