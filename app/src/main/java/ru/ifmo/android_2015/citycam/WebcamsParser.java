package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ru.ifmo.android_2015.citycam.model.Webcam;

/**
 * Created by ruslanabdulhalikov on 02.11.15.
 */
public class WebcamsParser {

    public List readStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return parseJson(reader);
        } catch (Exception e) {
            return null;
        } finally {
            reader.close();
        }
    }

    public List parseJson(JsonReader reader) throws IOException {
        List<Webcam> cams = new ArrayList<>();
        reader.beginObject();
        while (reader.hasNext()) {
            String token = reader.nextName();
            switch (token) {
                case "status":
                    if (!reader.nextString().equals("ok")) {
                        throw new IOException("Ivalid data");
                    }
                    break;
                case "webcams":
                    cams = parseWebcamsInfo(reader);
            }
        }
        reader.endObject();
        return cams;
    }

    public List parseWebcamsInfo(JsonReader reader) throws IOException {
        reader.beginObject();
        List<Webcam> cams = new ArrayList<>();

        while (reader.hasNext()) {
            String token = reader.nextName();
            switch (token) {
                case "webcam":
                    cams = parseWebcamsArray(reader);
                    break;
                default:
                    reader.nextInt();
                    break;
            }
        }
        reader.endObject();
        return cams;
    }

    public List parseWebcamsArray(JsonReader reader) throws IOException {
        reader.beginArray();
        List<Webcam> cams = new ArrayList<>();

        while (reader.hasNext()) {
            cams.add(parseOneWebcam(reader));
        }
        reader.endArray();
        return cams;
    }

    public Webcam parseOneWebcam(JsonReader reader) throws IOException {
        reader.beginObject();
        String imageUrl = null;
        String title = null;
        String city = null;
        double rating = 0.0;
        while (reader.hasNext()) {
            String token = reader.nextName();
            switch (token) {
                case "title":
                    title = reader.nextString();
                    break;
                case "city":
                    city = reader.nextString();
                    break;
                case "rating_avg":
                    rating = reader.nextDouble();
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
        return new Webcam(title, city, imageUrl, rating);
    }

    public Bitmap downloadBitmapImage(String imageUrl) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
        try {
            return BitmapFactory.decodeStream(conn.getInputStream());
        } finally {
            conn.disconnect();
        }
    }
}
