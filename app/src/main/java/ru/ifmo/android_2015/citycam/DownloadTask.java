package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.citycam.activities.CityCamActivity;
import ru.ifmo.android_2015.citycam.api.RestClient;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.WebCam;
import ru.ifmo.android_2015.citycam.model.WebCamsResult;

/**
 * @author Andreikapolin
 * @date 12.01.16
 */
public class DownloadTask extends AsyncTask<City, Integer, Bitmap> {

    private CityCamActivity activity;

    public enum Result {
        SUCCESS, ERROR, IN_PROGRESS
    }
    private Result result = null;

    private Bitmap bitmap;
    private WebCam webCam;

    public DownloadTask(CityCamActivity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        result = Result.IN_PROGRESS;

        ProgressBar progressView = (ProgressBar) activity.findViewById(R.id.progress);
        progressView.setVisibility(View.VISIBLE);
    }

    @Override
    protected Bitmap doInBackground(City... params) {
        City city = params[0];
        WebCamsResult webCamsResult = RestClient.webCams(city.latitude, city.longitude);
        if (webCamsResult == null) {
            result = Result.ERROR;
        } else {
            if (!webCamsResult.getWebcams().getWebcam().isEmpty()) {
                int size = webCamsResult.getWebcams().getWebcam().size();
                int id = (int) (Math.random() * size); // random webcam from chosen city
                webCam = webCamsResult.getWebcams().getWebcam().get(id);
                bitmap = constructBitmapFromURL(webCam.getPreviewUrl());
                result = Result.SUCCESS;
            } else {
                result = Result.ERROR;
            }
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        activity.updateUI(result, webCam, bitmap);
    }

    /**
     * Этот метод вызывается, когда новый объект Activity подключается к
     * данному таску после смены конфигурации.
     *
     * @param activity новый объект Activity
     */
    public void attachActivity(CityCamActivity activity) {
        this.activity = activity;
        activity.updateUI(result, webCam, bitmap);
    }

    private Bitmap constructBitmapFromURL(String src) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);
        } catch (IOException ignored) {}
        return bitmap;
    }

}
