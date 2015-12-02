package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.view.View;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.Image;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

public class CityCamDownload extends AsyncTask<City, Void, Integer> {

    private CityCamActivity activity;
    private Status status;

    public CityCamDownload(CityCamActivity act) {
        this.activity = act;
        this.status = Status.RUNNING;
    }

    public void attachActivity(CityCamActivity act) {
        this.activity = act;
        if (status == Status.FINISHED || status == Status.PENDING) {
            activity.progressView.setVisibility(View.GONE);
        }
    }

    @Override
    protected Integer doInBackground(City... params) {

        City city = params[0];

        try {
            moreInformation(city);
        } catch (Exception e) {
            status = Status.PENDING;
            return 42;
        }
        status = Status.FINISHED;
        return 0;
    }

    private void moreInformation(City c) throws Exception {
        HttpURLConnection httpurl = null;
        InputStreamReader input;
        JsonReader json;

        try {
            httpurl = (HttpURLConnection) Webcams.createNearbyUrl(c.latitude, c.longitude).openConnection();
            input = new InputStreamReader(httpurl.getInputStream());
            json = new JsonReader(input);
            String title = "";
            String prUrl = "";
            if (httpurl.getResponseCode() != 200){
                throw new Exception("Response code is not 200");
            }
            while (json.hasNext()) {

                if (json.peek() == JsonToken.BEGIN_OBJECT) {
                    json.beginObject();
                }

                String str = json.nextName();
                switch (str) {
                    case "status":
                        String s = json.nextString();
                        if (!s.equals("ok")) {
                            throw new Exception("Something wrong");
                        }
                        break;
                    case "webcams":
                        json.beginObject();
                        break;
                    case "webcam":
                        json.beginArray();
                        break;
                    case "count":
                        if (json.nextInt() == 0) {
                            throw new Exception("No camera");
                        }
                        break;
                    case "title":
                        title = json.nextString();
                        break;
                    case "preview_url":
                        prUrl = json.nextString();
                        break;
                    default:
                        json.skipValue();
                }
                if (json.peek() == JsonToken.END_OBJECT) {
                    activity.image = new Image(download(new URL(prUrl)),title);
                }

            }
        } catch (Exception e) {
            throw new Exception("ERROR!!!");
        } finally {
            if (httpurl != null) {
                httpurl.disconnect();
            }
        }
    }

    private Bitmap download(URL url) throws Exception {
        HttpURLConnection image = (HttpURLConnection) url.openConnection();
        InputStream stream = image.getInputStream();
        Bitmap view = BitmapFactory.decodeStream(stream);
        image.disconnect();
        return view;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        activity.progressView.setVisibility(View.VISIBLE);
    }

    protected void onPostExecute(Integer res) {
        super.onPostExecute(res);
        activity.progressView.setVisibility(View.GONE);
        if (res == 0) {
            activity.setImage();
        } else {
            activity.setErrorImage();
        }
    }

    private static final String TAG = "CityCamDownload";
}
