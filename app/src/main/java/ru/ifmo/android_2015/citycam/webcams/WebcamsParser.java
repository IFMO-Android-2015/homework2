package ru.ifmo.android_2015.citycam.webcams;

import android.util.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import ru.ifmo.android_2015.citycam.model.Webcam;

public  class WebcamsParser {
    private JsonReader reader;
    public WebcamsParser(InputStream in) throws UnsupportedEncodingException {
        this.reader = new JsonReader(new InputStreamReader(in,"UTF-8"));
    }

    public List parseJson() throws IOException {
        List<Webcam> cams = new ArrayList<>();
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "status":
                    if (!reader.nextString().equals("ok"))
                        throw new IOException("Invalid response returned by server");
                    break;
                case "webcams":
                    cams = parseWebcamsInfo();
            }
        }
        reader.endObject();
        return cams;
    }

    public List parseWebcamsInfo() throws IOException {
        reader.beginObject();
        List<Webcam> cams = new ArrayList<>();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "webcam":
                    cams = parseWebcamArray();
                    break;
                default:
                    reader.nextInt();
                    break;
            }
        }
        reader.endObject();
        return cams;
    }

    public List parseWebcamArray() throws IOException {
        reader.beginArray();
        List<Webcam> cams = new ArrayList<>();
        while (reader.hasNext()) cams.add(parseWebcam());
        reader.endArray();
        return cams;
    }

    public Webcam parseWebcam() throws IOException {
        reader.beginObject();
        String imageUrl = null;
        String title = null;
        double rating = 0.0;
        while (reader.hasNext()) {
            String token = reader.nextName();
            switch (token) {
                case "preview_url":
                    imageUrl = reader.nextString();
                    break;
                case "title":
                    title = reader.nextString();
                    break;
                case "rating_avg":
                    rating = reader.nextDouble();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return new Webcam(title, imageUrl, rating);
    }
}
