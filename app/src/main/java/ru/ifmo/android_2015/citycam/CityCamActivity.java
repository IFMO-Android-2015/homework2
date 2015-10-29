package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;

import ru.ifmo.android_2015.citycam.download.DownloadCamImageTask;
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

    private City city;

    private ImageView camImageView;
    private ProgressBar progressView;
    private TextView camName, camTitle, coordinates, latTitle, lat, lonTitle, lon;

    private DownloadCamImageTask downloadTask;

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
        camName = (TextView) findViewById(R.id.cam_name);
        camTitle = (TextView) findViewById(R.id.cam_title);
        coordinates = (TextView) findViewById(R.id.coordinates);
        latTitle = (TextView) findViewById(R.id.latitude_title);
        lat = (TextView) findViewById(R.id.latitude);
        lonTitle = (TextView) findViewById(R.id.longitude_title);
        lon = (TextView) findViewById(R.id.longitude);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);
        camName.setVisibility(View.INVISIBLE);

        if (savedInstanceState != null) {
            downloadTask = (DownloadCamImageTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask == null) {
            downloadTask = new DownloadCamImageTask(this);
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
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putString("CAM_NAME", camName.getText().toString());
        bundle.putString("CAM_TITLE", camTitle.getText().toString());
        bundle.putString("LATITUDE", lat.getText().toString());
        bundle.putString("LONGITUDE", lon.getText().toString());
        Drawable temp = camImageView.getDrawable();
        if (temp != null) {
            bundle.putParcelable("CAM_IMAGE", ((BitmapDrawable) temp).getBitmap());
        }
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        if (bundle != null) {
            camName.setText(bundle.getString("CAM_NAME"));
            camTitle.setText(bundle.getString("CAM_TITLE"));
            lat.setText(bundle.getString("LATITUDE"));
            lon.setText(bundle.getString("LONGITUDE"));
            if (downloadTask.getProgress() == DownloadCamImageTask.Progress.GettingImage ||
                    downloadTask.getProgress() == DownloadCamImageTask.Progress.GettingInfo) {
                progressView.setVisibility(View.VISIBLE);
                camName.setVisibility(View.INVISIBLE);
            } else {
                Bitmap bitmap = bundle.getParcelable("CAM_IMAGE");
                camImageView.setImageBitmap(bitmap);
                camImageView.setVisibility(View.VISIBLE);
                progressView.setVisibility(View.INVISIBLE);
                if (downloadTask.getProgress() == DownloadCamImageTask.Progress.Error) {
                    camName.setVisibility(View.INVISIBLE);
                } else {
                    coordinates.setVisibility(View.VISIBLE);
                    latTitle.setVisibility(View.VISIBLE);
                    lat.setVisibility(View.VISIBLE);
                    lonTitle.setVisibility(View.VISIBLE);
                    lon.setVisibility(View.VISIBLE);
                }
            }
        }
        super.onRestoreInstanceState(bundle);
    }

    private static final String TAG = "CityCam";
}
