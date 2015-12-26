package ru.ifmo.android_2015.citycam;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Created by dominica on 15.12.15.
 */
public class DownloadAsyncTask extends AsyncTask<City, String, Bitmap> {
    CityCamActivity activity;

    String webcamName = "";
    double latitude = 0;
    double longitude = 0;
    int viewCount = 0;
    URL previewUrl = null;
    String userName = "";

    URL jsonUrl = null;
    private Bitmap bitmap;
    boolean downloadCompleted = false;

    public DownloadAsyncTask(CityCamActivity activity) {
        this.activity = activity;
    }

    void attachActivity(CityCamActivity activity) {
        this.activity = activity;
        updateUI();
    }

    @Override
    protected Bitmap doInBackground(City... params) {
        try {
            City city = params[0];
            jsonUrl = Webcams.createNearbyUrl(city.latitude, city.longitude);
            JsonReader reader = null;
            HttpURLConnection connection = (HttpURLConnection) jsonUrl.openConnection();
            connection.connect();
            reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
            if (readJson(reader)) {
                Log.d(TAG, "Webcamname = " + webcamName);
                Log.d(TAG, "url = " + previewUrl.toString());

                InputStream inStr = previewUrl.openStream();
                bitmap = BitmapFactory.decodeStream(inStr);

                Log.d(TAG, "doInBackground finished");

                return bitmap;
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(activity, activity.getResources().getString(R.string.warning_string),
                    Toast.LENGTH_LONG).show();
        }
        downloadCompleted = true;
        updateUI();
        Log.d(TAG, "Image set");
    }

    private void updateUI() {
        if (downloadCompleted) {
            if (bitmap != null) {
                activity.camImageView.setImageBitmap(bitmap);
            } else {
                activity.camImageView.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(),
                        R.drawable.connection_error));
            }
            activity.webcamNameView.append(webcamName);
            activity.userNameView.append(userName);
            activity.longitudeView.append(String.valueOf(longitude));
            activity.latitudeView.append(String.valueOf(latitude));
            activity.viewCountView.append(String.valueOf(viewCount));
            activity.progressView.setVisibility(View.INVISIBLE);
        }
    }

    private boolean readJson(JsonReader reader) throws IOException {

        boolean flag = false;
        reader.beginObject();
        while (reader.hasNext()) {
            if (reader.nextName().equals("webcams")) {
                flag = readWebcams(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return flag;
    }

    private boolean readWebcams(JsonReader reader) throws IOException {
        boolean flag = true;

        reader.beginObject();
        while (reader.hasNext()) {
            String nextName = reader.nextName();
            if (nextName.equals("count")) {
                if (reader.nextInt() == 0) {
                    flag = false;
                }
            } else if (flag && nextName.equals("webcam")) {
                readWebcam(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return flag;
    }

    private void readWebcam(JsonReader reader) throws IOException {
        reader.beginArray();
        reader.beginObject();
        while (reader.hasNext()) {
            String nextName = reader.nextName();
            switch (nextName) {
                case "title":
                    webcamName = reader.nextString();
                    break;
                case "preview_url":
                    previewUrl = new URL(reader.nextString());
                    break;
                case "latitude":
                    latitude = reader.nextDouble();
                    break;
                case "longitude":
                    longitude = reader.nextDouble();
                    break;
                case "user":
                    userName = reader.nextString();
                    break;
                case "view_count":
                    viewCount = reader.nextInt();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        while (reader.hasNext()) {
            reader.skipValue();
        }
        reader.endArray();
        return;
    }

    private static final String TAG = DownloadAsyncTask.class.getSimpleName();

}
