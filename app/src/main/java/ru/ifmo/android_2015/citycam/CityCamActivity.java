package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.util.List;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.Webcam;
import ru.ifmo.android_2015.citycam.webcams.Webcams;
import ru.ifmo.android_2015.citycam.webcams.WebcamsParser;

/**
 * Экран, показывающий веб-камеру одного выбранного города.
 * Выбранный город передается в extra параметрах.
 */
public class CityCamActivity extends AppCompatActivity {

    /**
     * Обязательный extra параметр - объект City, камеру которого надо показать.
     */
    public static final String EXTRA_CITY = "city";
    private City city;
    private ImageView camImageView;
    private ProgressBar progressView;
    private DownloadTask task;
    private TextView view_title, view_city, view_rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        setContentView(R.layout.activity_city_cam);
        camImageView = (ImageView) findViewById(R.id.cam_image);
        progressView = (ProgressBar) findViewById(R.id.progress);
        view_title = (TextView) findViewById(R.id.text_title);
        view_rating = (TextView) findViewById(R.id.text_rating);
        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);
        if (savedInstanceState != null) {
            task = (DownloadTask) getLastCustomNonConfigurationInstance();
        }
        if (savedInstanceState == null) {
            task = new DownloadTask(this);
            task.execute();
        } else {
            task.associate(this);
        }
    }

    public Object onRetainCustomNonConfigurationInstance() {
        return task;
    }

    private static final String TAG = "CityCam";

    private enum DownloadStatus {
        OK, DOWNLOADING, ERROR
    }

    private class DownloadTask extends AsyncTask<Void, Void, DownloadStatus> {
        private CityCamActivity myActivity;
        private DownloadStatus status = DownloadStatus.DOWNLOADING;
        private Webcam webcam = null;
        private Bitmap pic;

        private DownloadTask(CityCamActivity activity) {
            myActivity = activity;
        }

        protected void associate(CityCamActivity activity) {
            myActivity = activity;
            publishProgress();
        }

        @Override
        protected DownloadStatus doInBackground(Void... params) {
            try {
                HttpURLConnection connection = (HttpURLConnection)
                        Webcams.createNearbyUrl(city.latitude, city.longitude).openConnection();
                WebcamsParser parser = new WebcamsParser(new BufferedInputStream(connection.getInputStream()));
                List<Webcam> cams = parser.parseJson();
                if (cams == null || cams.isEmpty()) {
                    status = DownloadStatus.ERROR;
                    return status;
                } else {
                    webcam = cams.get(0);
                    pic = webcam.downloadBitmapImage();
                    connection.disconnect();
                }
            } catch (Exception ex) {
                status = DownloadStatus.ERROR;
                return status;
            }
            status = DownloadStatus.OK;
            return status;
        }

        @Override
        protected void onPostExecute(DownloadStatus st) {
            status = st;
            if (st == DownloadStatus.ERROR) {
                Toast.makeText(getApplicationContext(), "Download failed or no camera available", Toast.LENGTH_SHORT).show();
            }
            updateActivity();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            updateActivity();
        }

        private void updateActivity() {
            if (status == DownloadStatus.OK) {
                myActivity.progressView.setVisibility(View.GONE);
                myActivity.camImageView.setImageBitmap(pic);
                myActivity.view_title.setText(webcam.title);
                myActivity.view_rating.setText(Double.toString(webcam.rating));
            }
        }

    }
}
