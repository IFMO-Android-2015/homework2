package ru.ifmo.android_2015.citycam;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
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

            String response = getJSONResponse(cityURL);
            if (response == null) {
                Log.e(TAG, "Error while getting response!");
                return 0;
            }

            String previewURL = "";
            String title = "";
            int timestamp = 0;

            JsonReader reader = new JsonReader(new StringReader(response));

            reader.beginObject();
            while (!reader.nextName().equals("webcams")) {
                reader.skipValue();
            }

            reader.beginObject();
            while (reader.hasNext()) {
                if (reader.nextName().equals("webcam")) {
                    break;
                } else {
                    reader.skipValue();
                }
            }

            reader.beginArray();
            if (reader.hasNext()) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String nextName = reader.nextName();
                    if (nextName.equals("title")) {
                        title = reader.nextString();
                    } else if (nextName.equals("preview_url")) {
                        previewURL = reader.nextString();
                    } else if (nextName.equals("last_update")) {
                        timestamp = Integer.parseInt(reader.nextString());
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
            }
            reader.close();
            
            activity.camera = new Webcam(title, timestamp, new URL(previewURL));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }

        return activity.camera.updatePreview() ? 1 : 0;
    }

    private String getJSONResponse(URL url) {
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
                return sb.toString();
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
