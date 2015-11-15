
package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.Webcam;
import ru.ifmo.android_2015.citycam.webcams.Webcams;


public class GetCamInfo extends AsyncTask<City, Void, Integer> {
    private CityCamActivity activity;
    private Status status = Status.NOTLOADED;

    private enum Status {
        NOTLOADED, DOWNLOADING, READY, ERROR
    }

    public GetCamInfo(CityCamActivity activity) {
        this.activity = activity;
        this.status = Status.DOWNLOADING;
    }

    public void attachActivity(CityCamActivity activity) {
        this.activity = activity;
        if (status == Status.READY || status == Status.ERROR) {
            activity.progressView.setVisibility(activity.progressView.GONE);
        }
    }

    @Override
    protected Integer doInBackground(City... params) {
        City city = params[0];

        try {
            getInfo(city);
        } catch (Exception e) {
            Log.e(TAG, "Error loading camera data from " + params[0], e);
            status = Status.ERROR;
            return 1;
        }
        status = Status.READY;
        return 0;
    }

    private void getInfo(City city) throws Exception {
        HttpURLConnection url = null;
        InputStreamReader in = null;
        JsonReader jsonReader = null;
        Log.i(TAG, "Getting info");
        Webcam ans = null;

        try {
            url = (HttpURLConnection) Webcams.createNearbyUrl(city.latitude, city.longitude).openConnection();
            in = new InputStreamReader(url.getInputStream());
            jsonReader = new JsonReader(in);
            if (url.getResponseCode() != 200) {
                Log.e(TAG, "Bad response from server:" + url.getResponseCode());
                throw new IOException("Bad response from server:" + url.getResponseCode());
            }
            double rating = 0;
            ArrayList<Webcams> cams = new ArrayList<>();
            String previewURL = "";
            String timezone = "";
            String title = "";
            double timeOffset = 0;
            int count = 0;
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.BEGIN_OBJECT) {
                    jsonReader.beginObject();
                }
                String name = jsonReader.nextName();
                switch (name) {
                    case "status":
                        String status = jsonReader.nextString();
                        if (!status.equals("ok")) {

                            Log.e(TAG, "Status not ok");
                            throw new IOException("Request failed");
                        }
                        break;
                    case "webcams":
                        jsonReader.beginObject();
                        break;
                    case "count":
                        count = jsonReader.nextInt();
                        if (count == 0) {
                            Log.e(TAG, "No cams");
                            throw new Exception("No cams");
                        }
                        break;
                    case "webcam":
                        jsonReader.beginArray();
                        break;
                    case "timezone":
                        timezone = jsonReader.nextString();
                        break;
                    case "timezone_offset":
                        timeOffset = jsonReader.nextDouble();
                        break;
                    case "rating_avg":
                        rating = jsonReader.nextDouble();
                        break;
                    case "preview_url":
                        previewURL = jsonReader.nextString();
                        break;
                    case "title":
                        title = jsonReader.nextString();
                        break;
                    default:
                        jsonReader.skipValue();
                        break;
                }
                if (jsonReader.peek() == JsonToken.END_OBJECT) {
                    ans = new Webcam(downloadPreview(new URL(previewURL)), title, rating, timezone, timeOffset);
                    activity.webcam = ans;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            throw new IOException("Error during getting info");
        } finally {
            if (url != null) {
                url.disconnect();
            }
        }
        Log.i(TAG, "Info ready");
    }

    private Bitmap downloadPreview(URL url) throws IOException {
        HttpURLConnection imageDl = (HttpURLConnection) url.openConnection();
        InputStream imageStream = imageDl.getInputStream();
        Bitmap newView = BitmapFactory.decodeStream(imageStream);
        imageDl.disconnect();
        return newView;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.progressView.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        activity.progressView.setVisibility(View.GONE);
        if (result == 1) {
            activity.showEmpty();
            Toast.makeText(activity, "Очень жаль", Toast.LENGTH_SHORT).show();
        } else {
            activity.showWebcam();
        }
    }

    private static final String TAG = "GetData";
}
