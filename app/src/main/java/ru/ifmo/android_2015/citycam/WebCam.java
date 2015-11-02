package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by daniil on 02.11.15.
 */
public class WebCam {
    Bitmap image;
    String id;
    String title;
    String latitude;
    String longitude;
    String city;
    String previewUrl;
    String dayLightPreviewUrl;

    WebCam(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String term = jsonReader.nextName();
            switch (term) {
                case "webcamid":
                    this.id = jsonReader.nextString();
                    break;
                case "title":
                    this.title = jsonReader.nextString();
                    break;
                case "latitude":
                    this.latitude = jsonReader.nextString();
                    break;
                case "longitude":
                    this.longitude = jsonReader.nextString();
                    break;
                case "city":
                    this.city = jsonReader.nextString();
                    break;
                case "preview_url":
                    this.previewUrl = jsonReader.nextString();
                    break;
                case "":
                    this.dayLightPreviewUrl = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }

    public void loadImage() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (new URL(previewUrl)).openConnection();
        InputStream inputStream = connection.getInputStream();

        setImage(BitmapFactory.decodeStream(inputStream));

        if (inputStream != null) {
            inputStream.close();
        }

        connection.disconnect();
    }

    public void setImage(Bitmap b) {
        this.image = b;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public void setDayLightPreviewUrl(String dayLightPreviewUrl) {
        this.dayLightPreviewUrl = dayLightPreviewUrl;
    }
}
