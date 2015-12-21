package ru.ifmo.android_2015.citycam.util;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import ru.ifmo.android_2015.citycam.R;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

public class DownloadPhotoTask extends AsyncTask<City, Integer, Void> {
    private static final String TAG = "DownloadPhoto";

    private Activity activity;
    private List<Webcam> webcamList;

    public void attach(Activity activity) {
        this.activity = activity;
        attach();
    }

    public void attach() {
        ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.progress);
        ImageView imageView = (ImageView) activity.findViewById(R.id.cam_image);

        // task done?
        if (webcamList != null) {
            progressBar.setVisibility(View.INVISIBLE);
            if (webcamList.size() > 0) {
                imageView.setImageBitmap(webcamList.get(0).getPreviewImage());
            } else {
                Toast.makeText(activity, "Can't download photo :(", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void detach() {
        this.activity = null;

    }

    @Override
    protected Void doInBackground(City... params) {
        Log.d(TAG, "doInbackground(City...)");
        try {
            City city = params[0];
            downloadPhotos(city);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    private void downloadPhotos(City city) throws IOException {
        URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);
        Log.d(TAG, "Downloading JSON from " + url);
        HttpURLConnection connection = null;
        InputStream stream = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            int response = connection.getResponseCode();
            Log.d(TAG, "Response code is: " + response);
            stream = connection.getInputStream();
            webcamList = JSONParser.readJSONStream(stream);
            for (Webcam webcam : webcamList) {
                webcam.downloadPhoto();
            }
        } finally {
            if (connection == null) {
                connection.disconnect();
            }
            if (stream == null) {
                stream.close();
            }
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        attach();
    }
}
