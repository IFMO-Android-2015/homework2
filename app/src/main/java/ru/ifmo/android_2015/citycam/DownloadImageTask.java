package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;

import java.io.InputStreamReader;
import java.net.URL;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Created by qurbonzoda on 09.12.15.
 */
public class DownloadImageTask extends AsyncTask<City, Void, Bitmap> {

    private CityCamActivity activity;

    DownloadImageTask(CityCamActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Bitmap doInBackground(City... city) {
        try {
            return throwableDoInBackground(city[0]);
        } catch (Exception e) {
            Log.d(CityCamActivity.TAG, e.toString());
            return null;
        }
    }

    private Bitmap throwableDoInBackground(City city) throws Exception {
        Log.d(CityCamActivity.TAG, "Downloading JSON...");
        URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);
        JsonReader jsonReader = new JsonReader(new InputStreamReader(url.openStream()));

        Log.d(CityCamActivity.TAG, "Reading JSON...");
        jsonReader.beginObject();
        while (jsonReader.hasNext() && !jsonReader.nextName().equals("webcams")) {
            jsonReader.skipValue();
        }
        jsonReader.beginObject();
        while (jsonReader.hasNext() && !jsonReader.nextName().equals("webcam")) {
            jsonReader.skipValue();
        }

        jsonReader.beginArray();
        jsonReader.beginObject();

        Log.d(CityCamActivity.TAG, "Webcam found...");


        URL preview_url = null;
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("preview_url")) {
                preview_url = new URL(jsonReader.nextString());
                break;
            }
            jsonReader.skipValue();
        }

        if (preview_url == null) {
            Log.d(CityCamActivity.TAG, "preview_url not found...");
            return null;
        }

        Log.d(CityCamActivity.TAG, "JSON read");
        Log.d(CityCamActivity.TAG, "Preview URL: " + preview_url.toString());

        return BitmapFactory.decodeStream(preview_url.openStream());
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        Log.d(CityCamActivity.TAG, "onPostExecute");

        if (bitmap == null) {
            Log.d(CityCamActivity.TAG, "Download unsuccessful");
        } else {
            Log.d(CityCamActivity.TAG, "Download successful");
            activity.progressView.setVisibility(View.INVISIBLE);
            activity.camImageView.setVisibility(View.VISIBLE);
            activity.camImageView.setImageBitmap(bitmap);
            activity.bitmap = bitmap;
        }
    }
    void attachActivity(CityCamActivity activity) {
        this.activity = activity;
    }

}
