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

    City city;

    public ImageView camImageView;
    public ProgressBar progressView;
    public TextView webcamNameView;
    public TextView latitudeView;
    public TextView longitudeView;
    public TextView  viewCountView;
    public TextView userNameView;

    private DownloadAsyncTask asyncTask;
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
        webcamNameView = (TextView) findViewById(R.id.webcam_name);
        latitudeView = (TextView) findViewById(R.id.latitude);
        longitudeView = (TextView) findViewById(R.id.longitude);
        viewCountView = (TextView) findViewById(R.id.view_count);
        userNameView = (TextView) findViewById(R.id.user_name);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.

        if (savedInstanceState != null) {
            asyncTask = (DownloadAsyncTask) getLastCustomNonConfigurationInstance();
        }
        if (asyncTask == null) {
            asyncTask = new DownloadAsyncTask(this);
            asyncTask.execute(city);
        } else {
            asyncTask.attachActivity(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return asyncTask;
    }

    private static final String TAG = "CityCam";
}
