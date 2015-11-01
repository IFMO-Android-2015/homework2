package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.DownloadWebcamImage;

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
    private DownloadWebcamImage downloadTask;
    private TextView title;
    private TextView info;

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
        title = (TextView) findViewById(R.id.title);
        info = (TextView) findViewById(R.id.info);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            downloadTask = (DownloadWebcamImage) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask == null) {
            downloadTask = new DownloadWebcamImage(this);
            downloadTask.execute(city);
        } else {
            downloadTask.attachActivity(this);
        }

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadTask;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        BitmapDrawable image = (BitmapDrawable) camImageView.getDrawable();
        if (image != null) {
            outState.putParcelable("image", image.getBitmap());
        }
        outState.putString("title", title.getText().toString());
        outState.putString("info", info.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (downloadTask.getState() != DownloadWebcamImage.State.INPROGRESS) {
                progressView.setVisibility(View.INVISIBLE);
                camImageView.setImageBitmap((Bitmap) savedInstanceState.get("image"));
            }
            title.setText(savedInstanceState.getString("title"));
            info.setText(savedInstanceState.getString("info"));
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private static final String TAG = "CityCam";
}