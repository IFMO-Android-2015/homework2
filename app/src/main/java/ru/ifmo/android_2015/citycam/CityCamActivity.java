package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.Webcam;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Экран, показывающий веб-камеру одного выбранного города.
 * Выбранный город передается в extra параметрах.
 */
public class CityCamActivity extends AppCompatActivity {

    private TextView textName;
    private TextView textCity;
    private TextView textRating;
    private TextView textViews;

    /**
     * Обязательный extra параметр - объект City, камеру которого надо показать.
     */
    public static final String EXTRA_CITY = "city";

    private City city;

    private ImageView camImageView;
    private ProgressBar progressView;

    private DownloadFileTask downloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_cam);

        textName = (TextView) findViewById(R.id.textName);
        textCity = (TextView) findViewById(R.id.textCity);
        textRating = (TextView) findViewById(R.id.textRating);
        textViews = (TextView) findViewById(R.id.textViews);

        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        camImageView = (ImageView) findViewById(R.id.cam_image);
        progressView = (ProgressBar) findViewById(R.id.progress);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
        if (savedInstanceState != null) {
            // Пытаемся получить ранее запущенный таск
            downloadTask = (DownloadFileTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask == null) {
            downloadTask = new DownloadFileTask(this);
            downloadTask.execute();
        } else {
            downloadTask.attachActivity(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadTask;
    }

    private static final String TAG = "CityCam";

    private void imageIsReady(Bitmap image) {
        camImageView.setImageBitmap(image);
    }

    enum DownloadState {
        DOWNLOADING,
        DONE,
        ERROR

        /*// ID строкового ресурса для заголовка окна прогресса
        final int titleResId;

        DownloadState(int titleResId) {
            this.titleResId = titleResId;
        }*/
    }

    static class DownloadFileTask extends AsyncTask<Void, Void, DownloadState> {

        private CityCamActivity activity;

        private DownloadState state = DownloadState.DOWNLOADING;

        private Bitmap image = null;
        private Webcam webcam = null;

        public DownloadFileTask(CityCamActivity activity) {
            this.activity = activity;
        }

        public void attachActivity(CityCamActivity activity) {
            this.activity = activity;
            publishProgress();
        }

        @Override
        protected DownloadState doInBackground(Void... params) {
            ByteArrayOutputStream jsonStream = new ByteArrayOutputStream();

            Log.d(TAG, "fetching json");

            //fetch JSON file with webcams
            try {
                if (activity != null) {
                    DownloadUtility.downloadFile(Webcams.createNearbyUrl(activity.city.latitude, activity.city.longitude), jsonStream);
                } else {
                    Log.e(TAG, "Activity is not attached");
                    state = DownloadState.ERROR;
                    return state;
                }
            } catch (IOException e) {
                Log.e(TAG, "Error downloading file: " + e);
                state = DownloadState.ERROR;
                return state;
            }

            //parse JSON and get the list of webcams
            List<Webcam> webcams;
            WebcamsParser parser = new WebcamsParser();
            try {
                webcams = parser.readJsonStream(new ByteArrayInputStream(jsonStream.toByteArray()));
            } catch (IOException e) {
                Log.e(TAG, "Error parsing json: " + e);
                state = DownloadState.ERROR;
                return state;
            }

            Log.d(TAG, "fetching image");

            if (webcams.size() > 0) {
                webcam = webcams.get(0);

                URL dowloadURL = webcams.get(0).previewURL;
                ByteArrayOutputStream imageStream = new ByteArrayOutputStream();

                try {
                    DownloadUtility.downloadFile(dowloadURL, imageStream);
                } catch (IOException e) {
                    Log.e(TAG, "Error downloading file: " + e);
                    state = DownloadState.ERROR;
                    return state;
                }

                image = BitmapFactory.decodeStream(new ByteArrayInputStream(imageStream.toByteArray())); //another method?

            } else {
                state = DownloadState.ERROR;
                return state;
            }

            Log.d(TAG, "done");

            state = DownloadState.DONE;
            return state;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            updateView();
        }

        @Override
        protected void onPostExecute(DownloadState state) {
            this.state = state;

            updateView();

            if (state == DownloadState.ERROR) {
                Toast.makeText(activity, "Failed to load webcams", Toast.LENGTH_SHORT).show();
            }
        }

        private void updateView() {
            if (state != DownloadState.DOWNLOADING) {
                activity.progressView.setVisibility(View.INVISIBLE);
                if (state == DownloadState.DONE) {
                    if (activity != null) {
                        activity.imageIsReady(image);
                        activity.textName.setText(activity.getResources().getString(R.string.webcam_name) + " " + webcam.title);
                        activity.textRating.setText(activity.getResources().getString(R.string.webcam_rating) + " " + webcam.ratingAvg);
                        activity.textCity.setText(activity.getResources().getString(R.string.webcam_city) + " " + webcam.city);
                        activity.textViews.setText(activity.getResources().getString(R.string.webcam_views) + " " + webcam.viewCount);
                    }
                }
            }
        }
    }
}
