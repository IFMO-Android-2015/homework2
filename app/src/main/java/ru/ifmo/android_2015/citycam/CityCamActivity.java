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
    TextView error;
    ImageView camImageView;
    ProgressBar progressView;
    private WebCamAsyncTask webCamTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city== null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }
        setContentView(R.layout.activity_city_cam);
        error = (TextView)findViewById(R.id.error);
        error.setVisibility(View.INVISIBLE);
        camImageView = (ImageView) findViewById(R.id.cam_image);
        progressView = (ProgressBar) findViewById(R.id.progress);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            // Пытаемся получить ранее запущенный таск
            webCamTask = (WebCamAsyncTask) getLastCustomNonConfigurationInstance();
        }
        if (webCamTask == null) {
            // Создаем новый таск, только если не было ранее запущенного таска
            webCamTask = new WebCamAsyncTask(this);
            webCamTask.execute();
        } else {
            // Передаем в ранее запущенный таск текущий объект Activity
            webCamTask.attachActivity(this);
        }
    }

    public Object onRetainCustomNonConfigurationInstance() {
        return webCamTask;
    }





    private static final String TAG = "CityCam";
}
