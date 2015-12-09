package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Экран, показывающий веб-камеру одного выбранного города.
 * Выбранный город передается в extra параметрах.
 */
public class CityCamActivity extends AppCompatActivity {

    /**
     * Обязательный extra параметр - объект City, камеру которого надо показать.
     */
    public static final String EXTRA_CITY = "city";
    public static final String BITMAP = "bitmap";

    private City city;

    ImageView camImageView;
    ProgressBar progressView;

    DownloadImageTask downloadTask;
    Bitmap bitmap;

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

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            downloadTask = (DownloadImageTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask == null) {
            Log.d(CityCamActivity.TAG, "Creating task...");
            downloadTask = new DownloadImageTask(this);
            downloadTask.execute(city);
        } else {
            Log.d(CityCamActivity.TAG, "Attaching activity...");
            downloadTask.attachActivity(this);
        }
        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelable(BITMAP, bitmap);
    }
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        bitmap = state.getParcelable(BITMAP);
        if (bitmap != null) {
            camImageView.setVisibility(View.VISIBLE);
            camImageView.setImageBitmap(bitmap);
            progressView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadTask;
    }

    public static final String TAG = "CityCam";
}
