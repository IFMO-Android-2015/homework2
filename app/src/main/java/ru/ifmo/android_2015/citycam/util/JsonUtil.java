package ru.ifmo.android_2015.citycam.util;

import android.util.JsonReader;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by artem on 30.10.15.
 */
public class JsonUtil {
    public static String getImageURL(File jsonObject) throws IOException, JSONException {
        return getWebcamField(jsonObject, "preview_url");
    }

    private static String getWebcamField(File jsonObject, String fieldName) throws IOException, JSONException {
        String WebcamField = null;
        InputStream in = new FileInputStream(jsonObject);
        JsonReader reader = new JsonReader(new InputStreamReader(in));
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("webcams")) {
                reader.beginObject();
                while (reader.hasNext()) {
                    name = reader.nextName();
                    if (name.equals("webcam")) {
                        break;
                    } else {
                        reader.skipValue();
                    }
                }
                if (!name.equals("webcam")) {
                    throw new JSONException("webcam not found");
                }
                reader.beginArray();
                if (!reader.hasNext()) {
                    throw new JSONException("no webcams in this area");
                }
                reader.beginObject();
                while (reader.hasNext()) {
                    name = reader.nextName();
                    if (name.equals(fieldName)) {
                        break;
                    } else {
                        reader.skipValue();
                    }
                }
                if (!name.equals(fieldName)) {
                    throw new IOException("field <" + fieldName + "> not found");
                } else {
                    WebcamField = reader.nextString();
                    reader.close();
                    break;
                }
            } else {
                reader.skipValue();
            }
        }
        return WebcamField;
    }
}
