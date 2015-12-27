package ru.ifmo.android_2015.citycam;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.WebCam;

/**
 * Created by Богдан on 27.12.2015.
 */
enum DownloadState {
    DOWNLOADING,
    DONE,
    ERROR;
}

public class DownloadCity extends AsyncTask<Void, Integer, DownloadState> {

    private CityCamActivity activity;
    private DownloadState state = DownloadState.DOWNLOADING;
    private City city;
    private WebCam data;

    protected DownloadCity(final CityCamActivity activity, final City city) {
        this.activity = activity;
        this.city = city;
    }

    void updateView() {
        if (activity != null) {
            if (state == DownloadState.DONE) {
                activity.camImageView.setImageBitmap(data.image);
                activity.titleTextView.setText(data.title);
            } else {
                activity.titleTextView.setText("Мы не нашли камеру");
            }
            activity.progressView.setVisibility(View.GONE);
        }
    }

    protected void attachActivity(final CityCamActivity activity) {
        this.activity = activity;
        updateView();
    }

    @Override
    protected DownloadState doInBackground(Void... params) {
        Log.d("DT", "doInBackground");
        try {
            data = JSONReader.getCityData(city);
            data.image = BitmapReader.getBitmap(data.url);
            state = DownloadState.DONE;
        } catch (Exception e) {
            state = DownloadState.ERROR;
        }
        return state;
    }

    @Override
    protected void onPostExecute(DownloadState state) {
        updateView();
    }
}
