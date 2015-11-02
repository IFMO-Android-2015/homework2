package ru.ifmo.android_2015.citycam;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
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

     ImageView camImageView;
     ProgressBar progressView;
     TextView loadStateView, titleView;
     RatingBar ratingBar;

    private WebCamScope scope;

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
        ratingBar    = (RatingBar) findViewById(R.id.rate_bar);
        loadStateView= (TextView) findViewById(R.id.load_state_view);
        titleView    = (TextView) findViewById(R.id.title_view);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);


        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
        if (savedInstanceState != null) {
            scope = (WebCamScope) getLastCustomNonConfigurationInstance();
        }
        if (scope == null) {
            scope = new WebCamScope(this, city);
        } else {
            scope.attachActivity(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return scope;
    }

    private static final String TAG = "CityCam";
}
