package ru.ifmo.android_2015.citycam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
 * @author creed
 * @date 06.11.15
 */
public class WebCamLoaderTask extends AsyncTask<City, Integer, Bitmap> {
    private ProgressBar progressView;

    private CityCamActivity activity;
    private Context context;
    private ConnectivityManager manager;

    public enum DownloadResult {
        SUCCESS, ERROR, IN_PROGRESS, NO_INTERNET
    }
    private DownloadResult result = null;

    private Bitmap bitmap;
    private WebCam webCam;

    public WebCamLoaderTask(CityCamActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        manager = (ConnectivityManager)activity.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        result = DownloadResult.IN_PROGRESS;

        progressView = (ProgressBar)activity.findViewById(R.id.progress);
        progressView.setVisibility(View.VISIBLE);
    }

    @Override
    protected Bitmap doInBackground(City... params) {
        if (!isNetworkAvailable()) {
            result = DownloadResult.NO_INTERNET;
            return null;
        }
        City city = params[0];
        WebCamsResult webCamsResult = RestClient.webCams(city.latitude, city.longitude);
        if (webCamsResult == null) {
            result = DownloadResult.ERROR;
        } else {
            if (!webCamsResult.getWebcams().getWebcam().isEmpty()) {
                webCam = webCamsResult.getWebcams().getWebcam().get(0);
                bitmap = getBitmapFromURL(webCam.getPreviewUrl());
                result = DownloadResult.SUCCESS;
            } else {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
                result = DownloadResult.ERROR;
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

    private Bitmap getBitmapFromURL(String src) {
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

    private boolean isNetworkAvailable() {
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
