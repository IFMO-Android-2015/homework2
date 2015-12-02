package ru.ifmo.android_2015.citycam;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.Image;


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
    public ProgressBar progressView;
    public TextView title;
    private CityCamDownload data;
    public Image image;

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
        title = (TextView) findViewById(R.id.textView);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.

        if (savedInstanceState != null) {
            data = (CityCamDownload) getLastCustomNonConfigurationInstance();
        }

        if (data == null) {
            data = new CityCamDownload(this);
            data.execute(city);
        } else {
            data.attachActivity(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return data;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        image = (Image) savedInstanceState.get("camera");
        if (image == null) {
            setErrorImage();
        } else {
            setImage();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("camera", image);
    }

    public void setImage() {
        camImageView.setImageBitmap(image.getImage());
        title.setText(image.getTitle());
    }

    public void setErrorImage() {
        camImageView.setImageResource(R.drawable.so_bad);
        title.setText("Нет камеры");
    }

    private static final String TAG = "CityCam";
}
