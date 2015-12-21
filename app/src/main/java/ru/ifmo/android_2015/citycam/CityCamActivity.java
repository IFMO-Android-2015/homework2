package ru.ifmo.android_2015.citycam;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    public static final String TAG = "CityCam";

    private City city;

    ImageView camImageView;
    ProgressBar progressView;
    TextView textView;
    DownloadTask downloadTask;

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
        textView = (TextView) findViewById(R.id.webStatus);

        getSupportActionBar().setTitle(city.name);
        progressView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.INVISIBLE);

        if (savedInstanceState != null){
            downloadTask = (DownloadTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask != null){
            downloadTask.setCityCamActivity(this);
        } else {
            downloadTask = new DownloadTask(this);
            downloadTask.execute(city);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (downloadTask.downloadProgress != DownloadTask.DownloadProgress.DOWNLOADED){
            if (downloadTask.downloadProgress == DownloadTask.DownloadProgress.NO_WEBCAMERA){
                progressView.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.VISIBLE);
            }
        } else {
            progressView.setVisibility(View.INVISIBLE);
            camImageView.setImageBitmap(downloadTask.getBitmap());
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadTask;
    }
}
