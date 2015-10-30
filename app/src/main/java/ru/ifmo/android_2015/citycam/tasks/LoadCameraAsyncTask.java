package ru.ifmo.android_2015.citycam.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import ru.ifmo.android_2015.citycam.CityCamActivity;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.CameraDescription;
import ru.ifmo.android_2015.citycam.webcams.CityCamApi;

public class LoadCameraAsyncTask extends AsyncTask<City, CameraDescription, CameraDescription> {
    private CityCamActivity activity;
    private boolean jsonLoadErrorOccured;

    public LoadCameraAsyncTask(CityCamActivity activity) {
        this.activity = activity;
    }

    public void bindActivity(CityCamActivity activity) {
        this.activity = activity;
    }

    public void unbindActivity() {
        activity = null;
    }

    @Override
    protected CameraDescription doInBackground(City... params) {
        City city = params[0];
        CameraDescription cameraDescription = null;
        try {
            cameraDescription = CityCamApi.getCameraDescription(city.latitude, city.longitude);
            if (cameraDescription == null) {
                return null;
            }
            publishProgress(cameraDescription);
            cameraDescription.getPreviewImage().loadImage();
        } catch (IOException e) {
            jsonLoadErrorOccured = true;
            Log.w(TAG, "Error loading camera data from " + params[0], e);
            return null;
        }

        cameraDescription.getPreviewImage().loadImage();
        return cameraDescription;
    }

    @Override
    protected void onProgressUpdate(CameraDescription... descriptions) {
        CameraDescription description = descriptions[0];
        activity.updateCameraDescription(description);
    }

    @Override
    protected void onPostExecute(CameraDescription description) {
        if (!jsonLoadErrorOccured) {
            activity.updateCameraDescription(description);
        } else {
            activity.updateError();
        }
    }

    private static final String TAG = "LoadCameraTask";
}
