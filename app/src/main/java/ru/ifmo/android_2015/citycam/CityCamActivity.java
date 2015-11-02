package ru.ifmo.android_2015.citycam;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;


import ru.ifmo.android_2015.citycam.model.City;

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
    private Webcam webcam;

    private ImageView camImageView;
    private ProgressBar progressView;
    private DownloadFileTask downloadTask;
    private Progress currProgress;
    TextView countryTextView, viewCountTextView, ratingCountTextView, cityTextView, debugTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState != null) {
            city = savedInstanceState.getParcelable("city");
            webcam = savedInstanceState.getParcelable("webcam");
            currProgress = (Progress)savedInstanceState.getSerializable("progress");
        } else {
            city = getIntent().getParcelableExtra(EXTRA_CITY);
            webcam = new Webcam(city);
            currProgress = Progress.DOWNLOADING;
        }
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }
        setContentView(R.layout.activity_city_cam);
        camImageView = (ImageView) findViewById(R.id.cam_image);
        progressView = (ProgressBar) findViewById(R.id.progress);
        countryTextView = (TextView) findViewById(R.id.text2);
        viewCountTextView = (TextView) findViewById(R.id.text3);
        ratingCountTextView = (TextView) findViewById(R.id.text4);
        cityTextView = (TextView) findViewById(R.id.text1);
        debugTextView = (TextView) findViewById(R.id.debugText);
        getSupportActionBar().setTitle(city.name);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
        switch (currProgress) {
            case OK:
                ready();
                break;
            case DOWNLOADING:
                downloading();
                if (savedInstanceState != null) {
                    downloadTask = (DownloadFileTask) onRetainCustomNonConfigurationInstance();
                }
                if (downloadTask == null) {
                    downloadTask = new DownloadFileTask(this);
                    downloadTask.execute();
                } else {
                    downloadTask.attachActivity(this);
                }
                break;
            default:
                error(currProgress);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("webcam", webcam);
        outState.putParcelable("city", city);
        outState.putSerializable("progress", currProgress);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadTask;
    }

    enum Progress {
        OK, NO_WEBCAM, NO_CONNECTION, DOWNLOADING, JSON_ERROR
    }

    class DownloadFileTask extends AsyncTask<Void, Integer, Progress> {

        CityCamActivity activity;

        DownloadFileTask(CityCamActivity a) {
            activity = a;
        }

        void attachActivity(CityCamActivity a) {
            activity = a;
        }

        protected Progress doInBackground(Void... params) {
            Log.i(TAG, "Task started");
            try {
                webcam.getInfo();
                if (webcam.value == null) {
                    return Progress.NO_WEBCAM;
                } else {
                    return Progress.OK;
                }
            } catch (IOException e) {
                Log.e("!", "Some error: " + e.getMessage());
                return Progress.NO_CONNECTION;
            } catch (JSONException e) {
                Log.e("!", "Some error: " + e.getMessage());
                return Progress.JSON_ERROR;
            }
        }

        @Override
        protected void onPostExecute(Progress p) {
            super.onPostExecute(p);
            activity.progressView.setVisibility(View.GONE);
            activity.currProgress = p;
            switch (p) {
                case OK:
                    activity.ready();
                    break;
                case DOWNLOADING:
                    activity.downloading();
                    break;
                default:
                    error(p);
            }
        }

    }



    void ready() {
        debugTextView.setVisibility(View.INVISIBLE);
        progressView.setVisibility(View.INVISIBLE);
        camImageView.setImageBitmap(webcam.picture);
        ratingCountTextView.setText("Rating: " + webcam.value[2]);
        countryTextView.setText("Country: " + webcam.value[0]);
        viewCountTextView.setText("Views: " + webcam.value[1]);
        cityTextView.setText("City: " + webcam.city.name);
    }

    void downloading() {
        debugTextView.setVisibility(View.VISIBLE);
        debugTextView.setText("Waiting for response from server");
    }

    void error(Progress p) {
        progressView.setVisibility(View.INVISIBLE);
        switch (p) {
            case NO_WEBCAM:
                debugTextView.setText("There is no webcams");
                break;
            case NO_CONNECTION:
                debugTextView.setText("Some errors with connection");
                break;
            case JSON_ERROR:
                debugTextView.setText("Some errors with webcam");
                break;
            default:
                debugTextView.setText("Unexpected error");
        }
    }

    private static final String TAG = "CityCam";
}
