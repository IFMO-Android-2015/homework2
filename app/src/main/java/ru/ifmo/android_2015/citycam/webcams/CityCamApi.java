package ru.ifmo.android_2015.citycam.webcams;

import android.graphics.Bitmap;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CityCamApi {
    public static CameraDescription getCameraDescription(double latitude, double longitude) throws IOException {
        try {
            URL nearbyCameraListUrl = Webcams.createNearbyUrl(latitude, longitude);
            HttpURLConnection connection = (HttpURLConnection) nearbyCameraListUrl.openConnection();
            InputStream in = null;

            try {
                in = connection.getInputStream();
                JsonReader reader = new JsonReader(new InputStreamReader(in));
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    switch (name) {
                        case "status":
                            String status = reader.nextString();
                            if (!status.equals("ok")) {
                                throw new IOException("CityCam API error: status != 'ok'");
                            }
                            break;
                        case "webcams":
                            return readFirstCameraDescriptionFromWebcamsObject(reader);
                        default:
                            reader.skipValue();
                    }
                }
                reader.endObject();
            } finally {
                if (in != null) {
                    in.close();
                }
                connection.disconnect();
            }
        } catch (IOException | IllegalStateException e) {
            throw new IOException("CityCam API error", e);
        }

        throw new IOException("CityCam API error: unexpected response format");
    }

    private static CameraDescription readFirstCameraDescriptionFromWebcamsObject(JsonReader reader) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "count":
                    if (reader.nextInt() == 0) {
                        return null;
                    }
                    break;
                case "webcam":
                    return readFirstCameraDescription(reader);
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        throw new IOException("CityCam API error: no camera description found");
    }

    private static CameraDescription readFirstCameraDescription(JsonReader reader) throws IOException {
        reader.beginArray();
        if (reader.hasNext()) {
            return readCameraDescription(reader);
        } else {
            return null;
        }
    }

    private static CameraDescription readCameraDescription(JsonReader reader) throws IOException {
        reader.beginObject();
        String previewUrl = null, title = null;
        float averageRating = 0;

        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "title":
                    title = reader.nextString();
                    break;
                case "preview_url":
                    previewUrl = reader.nextString();
                    break;
                case "rating_avg":
                    averageRating = (float) reader.nextDouble();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new CameraDescription(title, previewUrl != null ? new CameraImage(new URL(previewUrl)): null, averageRating);
    }
}
