package ru.ifmo.android_2015.citycam;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import android.widget.TableLayout;
import android.widget.TextView;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.Webcam;
import ru.ifmo.android_2015.citycam.webcams.GetWebcamInfoTask;

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
    private TableLayout webcamInfoTable;
    private TextView textName;
    private TextView textViewCount;
    private TextView textException;

    private GetWebcamInfoTask getWebcamInfoTask;

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
        webcamInfoTable = (TableLayout) findViewById(R.id.webcamInfoTable);
        textName = (TextView) findViewById(R.id.textName);
        textViewCount = (TextView) findViewById(R.id.textViewCount);
        textException = (TextView) findViewById(R.id.textException);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);
        webcamInfoTable.setVisibility(View.GONE);
        textException.setVisibility(View.GONE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.

        if (savedInstanceState != null) {
            getWebcamInfoTask = (GetWebcamInfoTask) getLastCustomNonConfigurationInstance();
        }
        if (getWebcamInfoTask == null) {
            getWebcamInfoTask = new GetWebcamInfoTask(this);
            getWebcamInfoTask.execute(city);
        } else {
            getWebcamInfoTask.bindActivity(this);
            showWebcamInfo(getWebcamInfoTask.webcam);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public Object onRetainCustomNonConfigurationInstance() {
        return getWebcamInfoTask;
    }

    public void showWebcamInfo(Webcam webcam) {
        if (webcam != null) {
            progressView.setVisibility(View.GONE);
            if (webcam.exception == null) {
                webcamInfoTable.setVisibility(View.VISIBLE);
                textName.setText(webcam.title);
                textViewCount.setText(String.valueOf(webcam.viewCount));
                camImageView.setImageBitmap(webcam.ImageBitmap);
            } else {
                textException.setVisibility(View.VISIBLE);
                camImageView.setImageResource(R.drawable.bsod);
                textException.setText(webcam.exception.toString());
            }
        }
    }

    private static final String TAG = "CityCam";
}
