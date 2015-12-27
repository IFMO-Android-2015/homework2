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

    private City city;
    private DownloadCity downloadCity;
    protected ImageView camImageView;
    protected TextView titleTextView;
    protected ProgressBar progressView;

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
        titleTextView = (TextView) findViewById(R.id.title);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь код, инициирующий асинхронную загрузку изображения с веб-камеры в выбранном городе.

        if (savedInstanceState != null) {
            downloadCity = (DownloadCity) getLastCustomNonConfigurationInstance();
        }
        if (downloadCity != null) {
            downloadCity.attachActivity(this);
        } else {
            downloadCity = new DownloadCity(this, city);
            downloadCity.execute();
        }
    }

    public Object onRetainCustomNonConfigurationInstance() {
        return downloadCity;
    }

    private static final String TAG = "CityCam";
}