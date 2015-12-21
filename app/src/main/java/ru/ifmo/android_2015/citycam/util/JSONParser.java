package ru.ifmo.android_2015.citycam.util;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JSONParser {
    public static List<Webcam> readJSONStream(InputStream stream) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(stream));
        try {
            return readResponse(reader);
        } finally {
            reader.close();
        }
    }

    public static List<Webcam> readResponse(JsonReader reader) throws IOException {
        List<Webcam> list = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("webcams")) {
                list = readWebcams(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return list;
    }

    private static List<Webcam> readWebcams(JsonReader reader) throws IOException {
        List<Webcam> list = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("webcam")) {
                list = readWebcamArray(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return list;
    }

    private static List<Webcam> readWebcamArray(JsonReader reader) throws IOException {
        List<Webcam> list = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            list.add(readWebcam(reader));
        }
        reader.endArray();

        return list;
    }

    private static Webcam readWebcam(JsonReader reader) throws IOException {
        String previewUrl = "";

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("preview_url")) {
                previewUrl = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new Webcam(previewUrl);
    }
}
