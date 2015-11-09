package ru.ifmo.android_2015.citycam;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.Webcam;

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
    private TextView title;
    private TextView info;
    private GetCamInfo getCamInfo;

    public ProgressBar progressView;
    public Webcam webcam;

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
        title = (TextView) findViewById(R.id.textTitle);
        info = (TextView) findViewById(R.id.textInfo);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.

        if (savedInstanceState != null) {
            getCamInfo = (GetCamInfo) getLastCustomNonConfigurationInstance();
        }
        if (getCamInfo == null) {
            getCamInfo = new GetCamInfo(this);
            getCamInfo.execute(city);
        } else {
            getCamInfo.attachActivity(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return getCamInfo;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webcam = (Webcam) savedInstanceState.get("camera");
        if (webcam != null) {
            showWebcam();
        } else {
            showEmpty();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("camera", webcam);
    }

    public void showEmpty() {
        camImageView.setImageResource(R.drawable.error1);
        title.setText("Нет камер");
        info.setText("Нет информации о камере");
    }

    public void showWebcam() {
        camImageView.setImageBitmap(webcam.getPreview());
        title.setText(webcam.getTitle());
        String inf = "Рейтинг: "+webcam.getRating() + "\nTimezone: " + webcam.getTimezone() + "\nGMT: " + webcam.getTimeOffset();
        info.setText(inf);
    }

    private static final String TAG = "CityCam";
}
