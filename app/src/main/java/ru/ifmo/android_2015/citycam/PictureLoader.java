package ru.ifmo.android_2015.citycam;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

import java.io.*;
import java.net.*;


class PictureLoader extends AsyncTask<Void, Integer, Bitmap> {
    private City city;
    private CityCamActivity currentActivity;
    private String message;
    Bitmap img = null;

    PictureLoader(CityCamActivity currentActivity, City city) {
        this.currentActivity = currentActivity;
        this.city = city;
    }

    void activityUpdate(CityCamActivity newActivity) {
        currentActivity = newActivity;
    }

    protected Bitmap doInBackground(Void... ignore) {
        HttpURLConnection url = null, conn = null;

        try {
            url = (HttpURLConnection) Webcams.createNearbyUrl(city.latitude, city.longitude).openConnection();
            InputStream in = url.getInputStream();
            JsonReader reader = new JsonReader(new InputStreamReader(in));

            reader.beginObject();
            String ss = reader.nextName();
            while (!ss.equals("webcam")) {
                if (ss.equals("webcams")) {
                    reader.beginObject();
                } else {
                    reader.skipValue();
                }
                ss = reader.nextName();
            }
            String title = "";
            String imgUrl = "";
            reader.beginArray();
            reader.beginObject();
            while (reader.hasNext()) {
                ss = reader.nextName();
                switch (ss) {
                    case "title":
                        title = reader.nextString();
                        break;
                    case "preview_url":
                        imgUrl = reader.nextString();
                        break;
                    default:
                        reader.skipValue();
                }
                if (!title.isEmpty() && !imgUrl.isEmpty()) {
                    break;
                }
            }
            reader.close();

            conn = (HttpURLConnection) new URL(imgUrl).openConnection();
            in = conn.getInputStream();
            img = BitmapFactory.decodeStream(in);
            message = title;
        } catch (Exception e) {
            message = "Ooops, something has gone wrong :(";
        } finally {
            if (url != null) {
                url.disconnect();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        publishProgress();
        return img;
    }

    @Override
    protected void onProgressUpdate(Integer... value) {
        ((TextView) currentActivity.findViewById(R.id.textView)).setText(message);
        currentActivity.message = message;
        currentActivity.img = img;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        currentActivity.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        ((ImageView) currentActivity.findViewById(R.id.cam_image)).setImageBitmap(bitmap);
    }
}