package ru.ifmo.android_2015.citycam.utils;

import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ru.ifmo.android_2015.citycam.model.Webcam;
/**
 * Created by heat_wave on 10/23/15.
 */
public class JSONHandler {


    public static List readJsonStream(InputStream in) throws IOException {

        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        reader.beginObject();
        try {
            while (reader.hasNext() && reader.peek() != JsonToken.BEGIN_OBJECT) {
                reader.skipValue();
            }
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (!name.equals("webcam")) {
                    reader.skipValue();
                }
                else {
                    break;
                }
            }
            if (reader.hasNext())
                return readWebcamsArray(reader);
            else
                return null;
        }
        finally{
            reader.close();
        }
    }

    public static List<Webcam> readWebcamsArray(JsonReader reader) throws IOException {
        List<Webcam> webcams = new ArrayList<Webcam>();
        reader.beginArray();
        while (reader.hasNext()) {
            webcams.add(readWebcam(reader));
        }
        reader.endArray();
        return webcams;
    }

    public static Webcam readWebcam(JsonReader reader) throws IOException {
        long unixTime = -1;
        String title = null;
        String country = null;
        String city = null;
        String imageUrl = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "title":
                    title = reader.nextString();
                    break;
                case "country":
                    country = reader.nextString();
                    break;
                case "city":
                    city = reader.nextString();
                    break;
                case "last_update":
                    unixTime = reader.nextLong();
                    break;
                case "preview_url":
                    imageUrl = reader.nextString();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return new Webcam(title, city + ", " + country, unixTime, imageUrl);
    }


    private JSONHandler() {}

}