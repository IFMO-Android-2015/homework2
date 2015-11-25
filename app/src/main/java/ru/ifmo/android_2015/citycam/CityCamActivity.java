package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
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
public class CityCamActivity extends Activity {

    /**
     * Обязательный extra параметр - объект City, камеру которого надо показать.
     */
    public static final String EXTRA_CITY = "city";

    private City city;

    private ImageView camImageView;
    private ProgressBar progressView;
    private PictureLoader currTask;
    String message;
    Bitmap img;
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




        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
        if (savedInstanceState != null) {
            MyBundle bun = (MyBundle) getLastNonConfigurationInstance();
            currTask = bun.task;
            img = bun.img;
            message = bun.title;
            camImageView.setImageBitmap(img);
            ((TextView) findViewById(R.id.textView)).setText(message);
            progressView.setVisibility(View.INVISIBLE);
            currTask.activityUpdate(this);
        } else {
            progressView.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.textView)).setText(R.string.load);
            currTask = new PictureLoader(this, city);
            currTask.execute();
        }

    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return new MyBundle(currTask, img, message);
    }
    private static final String TAG = "CityCam";
}
