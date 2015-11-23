package ru.ifmo.android_2015.citycam.webcams;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import ru.ifmo.android_2015.citycam.CityCamActivity;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.Webcam;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetWebcamInfoTask extends AsyncTask<City, Void, Webcam> {
    private CityCamActivity activity;
    public Webcam webcam;

    public GetWebcamInfoTask(CityCamActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Webcam doInBackground(City... params) {
        try {
            Webcam webcam = getWebcamInfo(params[0].latitude, params[0].longitude);
            populateWebcamWithImage(webcam);
            return webcam;
        } catch (Exception e) {
            return new Webcam(e);
        }
    }

    @Override
    protected void onPostExecute(Webcam webcam) {
        this.webcam = webcam;
        activity.showWebcamInfo(webcam);
    }

    public void bindActivity(CityCamActivity activity) {
        this.activity = activity;
    }

    private Webcam getWebcamInfo(double latitude, double longitude) throws IOException {
        HttpURLConnection conn = null;
        InputStream is = null;
        Webcam webcam = null;
        try {
            URL url = Webcams.createNearbyUrl(latitude, longitude);
            conn = (HttpURLConnection) url.openConnection();
            is = conn.getInputStream();
            webcam = parseJson(is);
        } finally {
            if (is != null) {
                is.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return webcam;
    }

    private void populateWebcamWithImage(Webcam webcam) throws IOException {
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL url = new URL(webcam.ImageUrl);
            conn = (HttpURLConnection) url.openConnection();
            is = conn.getInputStream();
            webcam.ImageBitmap = BitmapFactory.decodeStream(is);
        } finally {
            if (is != null) {
                is.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private Webcam parseJson(InputStream is) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(is));
        reader.beginObject();
        while (!reader.nextName().equals("webcams")) {
            reader.skipValue();
        }
        //webcams object
        reader.beginObject();
        while (!reader.nextName().equals("webcam")) {
            reader.skipValue();
        }
        //webcam array
        reader.beginArray();
        //get info from 1st el, skip others
        reader.beginObject();
        Webcam res = extractInfoFromWebcamJsonObject(reader);
        reader.close();
        return res;
    }

    private Webcam extractInfoFromWebcamJsonObject(JsonReader reader) throws IOException {
        Webcam webcam = new Webcam();
        while (reader.hasNext()) {
            String key = reader.nextName();
            switch (key) {
                case "preview_url":
                    webcam.ImageUrl = reader.nextString();
                    break;
                case "title":
                    webcam.title = reader.nextString();
                    break;
                case "view_count":
                    webcam.viewCount = reader.nextInt();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        return webcam;
    }
}
