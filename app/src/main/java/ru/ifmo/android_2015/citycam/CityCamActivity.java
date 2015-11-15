package ru.ifmo.android_2015.citycam;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.Webcam;

/**
 * Экран, показывающий веб-камеру одного выбранного города.
 * Выбранный город передается в extra параметрах.
 */
public class CityCamActivity extends AppCompatActivity {

    private static final String TAG = "CityCam";

    /**
     * Обязательный extra параметр - объект City, камеру которого надо показать.
     */
    public static final String EXTRA_CITY = "city";


    public City city;
    public Webcam camera;

    public ImageView camImageView;
    public ProgressBar progressView;
    public TextView camTitle;
    public TextView camLastUpdate;

    private DownloadWebcamTask downloadWebcamTask;

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
        camTitle = (TextView) findViewById(R.id.cam_title);
        camLastUpdate = (TextView) findViewById(R.id.cam_last_update);

        getSupportActionBar().setTitle(city.name);

        if (savedInstanceState != null) {
            downloadWebcamTask = (DownloadWebcamTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadWebcamTask == null) {
            downloadWebcamTask = new DownloadWebcamTask(this);
            downloadWebcamTask.execute();
        } else {
            downloadWebcamTask.attachActivity(this);
        }

    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadWebcamTask;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        camera = (Webcam) savedInstanceState.get("camera");
        if (camera != null) {
            showCamera();
        } else {
            showEmptyCamera();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("camera", camera);
    }

    /**
     * Отобразить превью и поля с информацией
     */
    public void showCamera() {
        camImageView.setImageBitmap(camera.preview);
        camTitle.setText(camera.title);
        camLastUpdate.setText(new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault()).format(camera.lastUpdate));
    }

    /**
     * Отобразить в случае отсутствия камеры
     */
    public void showEmptyCamera() {
        Toast.makeText(getApplicationContext(), "Не удалось загрузить камеру.", Toast.LENGTH_SHORT).show();
        camImageView.setVisibility(View.INVISIBLE);
        camTitle.setText("Камер не найдено.");
    }

}
