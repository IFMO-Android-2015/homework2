package ru.ifmo.android_2015.citycam;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.citycam.model.Webcam;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

public class DownloadWebcamTask extends AsyncTask<Void, Void, Integer> {

    private static final String TAG = "DownloadWebcamTask";

    private CityCamActivity activity;

    DownloadWebcamTask(CityCamActivity activity) {
        this.activity = activity;
    }

    void attachActivity(CityCamActivity activity) {
        this.activity = activity;
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
        if (result == 0) {
            activity.showEmptyCamera();
        } else {
            activity.showCamera();
        }
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Log.d(TAG, "Start downloading...");

        URL cityURL;
        try {
            cityURL = Webcams.createNearbyUrl(activity.city.latitude, activity.city.longitude);

            JSONObject response = getJSONResponse(cityURL);
            JSONArray webcams;
            if (response != null) {
                webcams = response.getJSONObject("webcams").getJSONArray("webcam");
            } else {
                Log.w(TAG, "Incorrect JSON response.");
                return 0;
            }

            String previewURL = webcams.getJSONObject(0).getString("preview_url");
            String title = webcams.getJSONObject(0).getString("title");
            int timestamp = Integer.parseInt(webcams.getJSONObject(0).getString("last_update"));

            activity.camera = new Webcam(title, timestamp, new URL(previewURL));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }

        return activity.camera.updatePreview() ? 1 : 0;
    }

    private JSONObject getJSONResponse(URL url) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200 || urlConnection.getResponseCode() == 201) {
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                return new JSONObject(sb.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }
}
