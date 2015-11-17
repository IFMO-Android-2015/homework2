package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.DownloadAsyncTask;

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
    private TextView name, title, coordinates, latTitle, latitude, lonTitle, longitude;
    private DownloadAsyncTask downloadTask;

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
        name = (TextView) findViewById(R.id.cam_name);
        title = (TextView) findViewById(R.id.cam_title);
        coordinates = (TextView) findViewById(R.id.coordinates);
        latTitle = (TextView) findViewById(R.id.latitude_title);
        latitude = (TextView) findViewById(R.id.latitude);
        lonTitle = (TextView) findViewById(R.id.longitude_title);
        longitude = (TextView) findViewById(R.id.longitude);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);
        name.setVisibility(View.INVISIBLE);
        if (savedInstanceState != null) {
            downloadTask = (DownloadAsyncTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask == null) {
            downloadTask = new DownloadAsyncTask(this);
            downloadTask.execute(city);
        } else
            downloadTask.updateView(this);
    }

    // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
    // в выбранном городе.
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadTask;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putString("NAME", name.getText().toString());
        bundle.putString("TITLE", title.getText().toString());
        bundle.putString("LATITUDE", latitude.getText().toString());
        bundle.putString("LONGITUDE", longitude.getText().toString());
        Drawable temp = camImageView.getDrawable();
        if (temp != null)
            bundle.putParcelable("IMAGE", ((BitmapDrawable) temp).getBitmap());
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        if (bundle != null) {
            name.setText(bundle.getString("NAME"));
            title.setText(bundle.getString("TITLE"));
            latitude.setText(bundle.getString("LATITUDE"));
            longitude.setText(bundle.getString("LONGITUDE"));
            if (downloadTask.progress == 1 || downloadTask.progress == 2) {
                progressView.setVisibility(View.VISIBLE);
                name.setVisibility(View.INVISIBLE);
            } else {
                Bitmap bitmap = bundle.getParcelable("IMAGE");
                camImageView.setImageBitmap(bitmap);
                progressView.setVisibility(View.INVISIBLE);
                camImageView.setVisibility(View.VISIBLE);
                if (downloadTask.progress == 3)
                    name.setVisibility(View.INVISIBLE);
                else {
                    coordinates.setVisibility(View.VISIBLE);
                    latTitle.setVisibility(View.VISIBLE);
                    latitude.setVisibility(View.VISIBLE);
                    lonTitle.setVisibility(View.VISIBLE);
                    longitude.setVisibility(View.VISIBLE);
                }
            }
        }
        super.onRestoreInstanceState(bundle);
    }

    private static final String TAG = "CityCam";
}
