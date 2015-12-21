package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.view.View;
import java.io.InputStreamReader;
import java.net.URL;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

public class DownloadTask extends AsyncTask<Void, Void, Bitmap> {

    private CityCamActivity activity;
    private City city;
    private Bitmap picture;
    private String camInfo;

    DownloadTask(CityCamActivity activity, City city) {
        this.activity = activity;
        this.city = city;
    }

    @Override
    protected Bitmap doInBackground(Void... args)  {
        try {
            URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);
            JsonReader jsonReader = new JsonReader(new InputStreamReader(url.openStream()));
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
            URL previewUrl = null;
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if (name.equals("preview_url")) {
                    previewUrl = new URL(jsonReader.nextString());
                    break;
                } else if (name.equals("title")) {
                    camInfo = jsonReader.nextString();
                } else {
                    jsonReader.skipValue();
                }
            }
            if (previewUrl == null) {
                camInfo = "It seems that there is no any camera in this city...";
                return null;
            }
            picture = BitmapFactory.decodeStream(previewUrl.openStream());
            return picture;
        } catch (Exception e) {
            camInfo = "Failed to get image";
            return null;
        }
    }

    @Override
    protected void onProgressUpdate(Void... args) {
        activity.camTextView.setText(camInfo);
        activity.camInfo = camInfo;
        activity.picture = picture;
    }

    @Override
    protected void onPostExecute(Bitmap picture) {
        super.onPostExecute(picture);
        activity.progressView.setVisibility(View.INVISIBLE);
        activity.camImageView.setVisibility(View.VISIBLE);
        activity.camImageView.setImageBitmap(picture);
        activity.camTextView.setText(camInfo);
        activity.picture = picture;
        activity.camInfo = camInfo;
    }

}
