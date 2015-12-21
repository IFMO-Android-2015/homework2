package ru.ifmo.android_2015.citycam;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by ilnarkadyrov on 12/21/15.
 */
public final class JsonParser {

    public static final String WEBCAMS = "webcams";
    public static final String WEBCAM = "webcam";

    public static Webcamera parseWebcam(InputStream inputStream) throws IOException{
        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));
        Webcamera webcam = null;
        jsonReader.beginObject();
        while (jsonReader.hasNext() && !jsonReader.nextName().equals(WEBCAMS)){
            jsonReader.skipValue();
        }
        jsonReader.beginObject();
        while (jsonReader.hasNext() && !jsonReader.nextName().equals(WEBCAM)){
            jsonReader.skipValue();
        }
        jsonReader.beginArray();
        if (jsonReader.hasNext()) {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                if (jsonReader.nextName().equals("preview_url")) {
                    webcam = new Webcamera(new URL(jsonReader.nextString()));
                    break;
                } else {
                    jsonReader.skipValue();
                }
            }
        }
        jsonReader.close();
        return webcam;
    }
}
