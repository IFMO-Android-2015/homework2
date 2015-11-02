package ru.ifmo.android_2015.citycam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import ru.ifmo.android_2015.citycam.util.DownloadUtil;
import ru.ifmo.android_2015.citycam.util.FileUtil;
import ru.ifmo.android_2015.citycam.util.JsonUtil;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Created by artem on 28.10.15.
 */

enum DownloadState {
    ERROR(-1), DONE(1), DOWNLOADING(0);
    private final int state;

    DownloadState(int state) {
        this.state = state;
    }
    public int getState() {
        return state;
    }

}

public class WebCamTask extends AsyncTask<Void, Integer, DownloadState> implements ProgressCallback {

    private CityCamActivity activity;
    private Context context;
    private String errorMessage = null;
    private DownloadState state = DownloadState.DOWNLOADING;
    private Bitmap image = null;
    private int progress;

    void updateView() {
        if (this.activity != null) {
            activity.progressView.setProgress(progress);
            if (errorMessage != null) {
                activity.info.setText(errorMessage);
                activity.progressView.setVisibility(View.INVISIBLE);
                activity.info.setVisibility(View.VISIBLE);
            }
            if (image != null) {
                activity.progressView.setVisibility(View.INVISIBLE);
                activity.camImageView.setImageBitmap(image);
                activity.BitmapKey = "currentBitmap";
            }
        }
    }

    WebCamTask(CityCamActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    void attachActivity(CityCamActivity activity) {
        this.activity = activity;
        updateView();
    }

    @Override
    protected void onPreExecute() {
        updateView();
    }

    @Override
    public void onProgressChanged(int progress) {
        publishProgress(progress);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (values.length > 0) {
            this.progress = values[values.length - 1];
        }
        updateView();
    }

    @Override
    protected void onPostExecute(DownloadState state) {
        this.state = state;
        if (state != DownloadState.ERROR) {
            this.progress = 100;
        }
        updateView();
    }

    @Override
    protected DownloadState doInBackground(Void... ignore) {
        try {
            getImageFromWebCam();
            state = DownloadState.DONE;
        } catch (JSONException e) {
            errorMessage = e.getMessage();
        } catch (Exception e) {
            Log.e("oi", "Error downloading file: " + e, e);
            state = DownloadState.ERROR;
        }
        return state;
    }

    void getImageFromWebCam() throws IOException, JSONException {
        URL requestUrl = Webcams.createNearbyUrl(activity.city.latitude, activity.city.longitude);
        File jsonFile = FileUtil.createTempExternalFile(context, "json");
        Log.d("Downloading", "json file");
        DownloadUtil.downloadFile(requestUrl, jsonFile);
        Log.d("Parsing", "json file");
        String imageURL = JsonUtil.getImageURL(jsonFile);
        File image = FileUtil.createTempExternalFile(context, "tmp");
        Log.d("Downloading", "image file");
        DownloadUtil.downloadFile(new URL(imageURL), image);
        this.image = BitmapFactory.decodeStream(new FileInputStream(image));
    }
}
