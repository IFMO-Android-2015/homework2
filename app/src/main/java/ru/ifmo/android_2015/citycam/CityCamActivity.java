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

import ru.ifmo.android_2015.citycam.download.DownloadWebcamImageTask;
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
    public static final String EXTRA_IMAGE = "image";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_INFO = "info";


    private City city;

    private ImageView camImageView;
    private ProgressBar progressView;
    private TextView camTitle, camInfo;

    private DownloadWebcamImageTask downloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }
        Log.w(TAG, "onCreate");

        setContentView(R.layout.activity_city_cam);
        camImageView = (ImageView) findViewById(R.id.cam_image);
        progressView = (ProgressBar) findViewById(R.id.progress);
        camTitle = (TextView) findViewById(R.id.title);
        camInfo = (TextView) findViewById(R.id.info);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.

        if (savedInstanceState != null) {
            downloadTask = (DownloadWebcamImageTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask == null) {
            downloadTask = new DownloadWebcamImageTask(this);
            downloadTask.execute(city);
        } else {
            downloadTask.attachActivity(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadTask;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        BitmapDrawable image = (BitmapDrawable) camImageView.getDrawable();
        if (image != null) {
            outState.putParcelable(EXTRA_IMAGE, image.getBitmap());
        }
        outState.putString(EXTRA_TITLE, camTitle.getText().toString());
        outState.putString(EXTRA_INFO, camInfo.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (downloadTask.getTaskStatus() == DownloadWebcamImageTask.Status.Error ||
                    downloadTask.getTaskStatus() == DownloadWebcamImageTask.Status.Finished) {
                progressView.setVisibility(View.INVISIBLE);
                camImageView.setImageBitmap((Bitmap) savedInstanceState.get(EXTRA_IMAGE));
            }
            camTitle.setText(savedInstanceState.getString(EXTRA_TITLE));
            camInfo.setText(savedInstanceState.getString(EXTRA_INFO));
        }
        super.onRestoreInstanceState(savedInstanceState);

    }

    private static final String TAG = "CityCam";
}
