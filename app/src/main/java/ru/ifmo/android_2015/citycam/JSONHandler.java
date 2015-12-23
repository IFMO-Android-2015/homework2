package ru.ifmo.android_2015.citycam;

import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anstanasia on 23.12.2015.
 */
public class JSONHandler {
    public static List<Webcam> readInputStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                Log.d(TAG, "!!!!!!!!!!!!!!!!!looking for webcams");
                if (name.equals("webcams")) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name2 = reader.nextName();
                        Log.d(TAG, "!!!!!!!!!!!!!!!!!looking for webcam array");
                        if (name2.equals("webcam")) {
                            break;
                        } else {
                            reader.skipValue();
                        }
                    }
                    if (reader.hasNext()) {
                        return readWebcamsArray(reader);
                    } else {
                        return null;
                    }
                } else {
                    reader.skipValue();
                }
            }
            return null;
        } finally {
            reader.close();
        }
    }

    public static List<Webcam> readWebcamsArray(JsonReader reader) throws IOException {
        List<Webcam> webcams = new ArrayList();
        reader.beginArray();
        while (reader.hasNext()) {
            webcams.add(readWebcams(reader));
        }
        reader.endArray();
        return webcams;
    }

    public static Webcam readWebcams(JsonReader reader) throws IOException {
        String title = "";
        long viewCount = -1;
        String user = "";
        String previewURL = "";

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("view_count")) {
                viewCount = reader.nextLong();
            } else if (name.equals("title")) {
                title = reader.nextString();
            } else if (name.equals("user")) {
                user = reader.nextString();
            } else if (name.equals("preview_url")) {
                previewURL = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new Webcam(title, user, viewCount, previewURL);
    }

    private static final String TAG = "JSONHandler";
}
