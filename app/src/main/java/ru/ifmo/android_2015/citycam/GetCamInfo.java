
package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcam;
import ru.ifmo.android_2015.citycam.webcams.Webcams;


public class GetCamInfo extends AsyncTask<City, Void, Integer> {
    private CityCamActivity activity;
    public Status status = Status.NOTLOADED;

    public enum Status {
        NOTLOADED, DOWNLOADING, READY, ERROR
    }

    public GetCamInfo(CityCamActivity activity) {
        this.activity = activity;
        this.status = Status.DOWNLOADING;
    }

    public void attachActivity(CityCamActivity activity) {
        this.activity = activity;
        if (status == Status.READY || status == Status.ERROR) {
            activity.progressView.setVisibility(activity.progressView.GONE);
        }
    }

    @Override
    protected Integer doInBackground(City... params) {
        City city = params[0];
        try {
            getInfo(city);
        } catch (Exception e) {
            Log.e(TAG, "Error loading camera data from " + params[0], e);
            status = Status.ERROR;
            return 1;
        }
        status = Status.READY;
        return 0;
    }

    private void getInfo(City city) throws Exception {
        HttpURLConnection url = null;
        InputStreamReader in = null;
        Log.i(TAG, "Getting info");
        Webcam ans = null;
        try {
            url = (HttpURLConnection) Webcams.createNearbyUrl(city.latitude, city.longitude).openConnection();
            in = new InputStreamReader(url.getInputStream());
            if (url.getResponseCode() != 200) {
                Log.e(TAG, "Bad response from server:" + url.getResponseCode());
                throw new IOException("Bad response from server:" + url.getResponseCode());
            }
            BufferedReader bufferedReader = new BufferedReader(in);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();
            JSONObject response = new JSONObject(stringBuilder.toString());
            JSONArray cams = response.getJSONObject("webcams").getJSONArray("webcam");
            int count = cams.length();
            Log.i(TAG, "Number of cams: " + count);
            if (count != 0) {
                Random random = new Random();
                JSONObject cam;
                cam = cams.getJSONObject(random.nextInt(count));
                Log.i(TAG, "Setting camera:" + cam.toString());
                String previewURL = cam.getString("preview_url");
                String title = cam.getString("title");
                String timezone = cam.getString("timezone");
                double rate = cam.getDouble("rating_avg");
                double offset = cam.getDouble("timezone_offset");
                ans = new Webcam(downloadPreview(new URL(previewURL)), title, rate, timezone, offset);
                activity.webcam = ans;
            } else {
                throw new IOException("No cams in the city " + city.name);
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            throw new IOException("Error during getting info");
        } finally {
            if (url != null) {
                url.disconnect();
            }
        }
        Log.i(TAG, "Info ready");
    }

    private Bitmap downloadPreview(URL url) throws IOException {
        HttpURLConnection imageDl = (HttpURLConnection) url.openConnection();
        InputStream imageStream = imageDl.getInputStream();
        Bitmap newView = BitmapFactory.decodeStream(imageStream);
        imageDl.disconnect();
        return newView;
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
        if (result == 1) {
            activity.showEmpty();
            Toast.makeText(activity, "Очень жаль", Toast.LENGTH_SHORT).show();
        } else {
            activity.showWebcam();
        }
    }

    private static final String TAG = "GetData";
}
