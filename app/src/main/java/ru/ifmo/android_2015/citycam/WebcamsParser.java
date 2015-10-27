package ru.ifmo.android_2015.citycam;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ru.ifmo.android_2015.citycam.model.Webcam;

/**
 * Created by ruslanthakohov on 26/10/15.
 */
public class WebcamsParser {
    public List<Webcam> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readResponse(reader);
        } finally {
            reader.close();
        }
    }

    public List<Webcam> readResponse(JsonReader reader) throws IOException {
        List<Webcam> list = new ArrayList<>();

        reader.beginObject();

        while (reader.hasNext()) {
            String name = reader.nextName();

            if (name.equals("status")) {
                if (!reader.nextString().equals("ok")) {
                    throw new IOException("Corrupted response");
                }
            } else if (name.equals("webcams")) {
                list = readWebcamsInfo(reader);
            } else {
                throw new IOException("Corrupted response");
            }
        }

        reader.endObject();

        return list;
    }

    public List<Webcam> readWebcamsInfo(JsonReader reader) throws  IOException {
        reader.beginObject();

        List<Webcam> list = new ArrayList<>();

        while (reader.hasNext()) {
            String name = reader.nextName();

            if (name.equals("count")) {
                reader.nextInt();
            } else if (name.equals("page")) {
                reader.nextInt();
            } else if (name.equals("per_page")) {
                reader.nextInt();
            } else if (name.equals("webcam")) {
                list = readWebcamsArray(reader);
            }
        }

        reader.endObject();

        return list;
    }

    public List<Webcam> readWebcamsArray(JsonReader reader) throws IOException {
        reader.beginArray();

        List<Webcam> list = new ArrayList<>();

        while (reader.hasNext()) {
            list.add(readWebcam(reader));
        }

        reader.endArray();

        return list;
    }

    public Webcam readWebcam(JsonReader reader) throws IOException { //corrupted webcam?
        reader.beginObject();

        String title = null;
        long viewCount = -1;
        String city = null;
        double ratingAvg = -1.0;
        URL previewURL = null;

        while (reader.hasNext()) {
            String name = reader.nextName();

            if (name.equals("title")) {
                title = reader.nextString();
            } else if (name.equals("view_count")) {
                viewCount = reader.nextLong();
            } else if (name.equals("city")) {
                city = reader.nextString();
            } else if (name.equals("rating_avg")) {
                ratingAvg = reader.nextDouble();
            } else if (name.equals("preview_url")) {
                previewURL = new URL(reader.nextString());
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();

        return new Webcam(title, viewCount, city, ratingAvg, previewURL);
    }
}
