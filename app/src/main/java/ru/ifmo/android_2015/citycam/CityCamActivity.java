package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

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

    public static final String CAM_IMAGE_VIEW = "camImageView";
    public static final String TITLE = "title";
    public static final String LAST_UPDATE = "lastUpdate";

    private City city;

    private ImageView camImageView;
    private ProgressBar progressView;
    private TextView title;
    private TextView lastUpdate;
    private GetWebcamView downloadTask;

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

        title = (TextView) (this.findViewById(R.id.title_label));
        lastUpdate = (TextView) (this.findViewById(R.id.last_update_label));

        if (savedInstanceState != null) {
            downloadTask = (GetWebcamView) getLastCustomNonConfigurationInstance();
        }

        if (downloadTask == null) {
            downloadTask = new GetWebcamView(this);
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
        if (downloadTask.getState() == GetWebcamView.DownloadState.DONE) {
            Drawable temp = camImageView.getDrawable();
            outState.putParcelable(CAM_IMAGE_VIEW, ((BitmapDrawable) temp).getBitmap());
        }
        outState.putString(TITLE, title.getText().toString());
        outState.putString(LAST_UPDATE, lastUpdate.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (downloadTask.getState() == GetWebcamView.DownloadState.DONE) {
                Bitmap temp = savedInstanceState.getParcelable(CAM_IMAGE_VIEW);
                camImageView.setImageBitmap(temp);
                lastUpdate.setVisibility(View.VISIBLE);
            }
            if (!(downloadTask.getState() == GetWebcamView.DownloadState.DOWNLOADING)) {
                progressView.setVisibility(View.GONE);
            }
            title.setText(savedInstanceState.getString(TITLE));
            lastUpdate.setText(savedInstanceState.getString(LAST_UPDATE));
            super.onRestoreInstanceState(savedInstanceState);
        }
    }

    public void errorOccurred() {
        title.setText(R.string.load_error);
    }

    public void printImage(Webcam webcam) {
        if (webcam.getImage() == null) {
            this.title.setText(R.string.no_webcams_error);
        } else {
            this.camImageView.setImageBitmap(webcam.getImage());
            this.title.setText(webcam.getTitle());
            this.lastUpdate.setVisibility(View.VISIBLE);
            DateFormat f = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault());
            this.lastUpdate.setText(f.format(webcam.getLastUpdate()));
        }
        this.progressView.setVisibility(View.GONE);
    }

    private static final String TAG = "CityCam";
}