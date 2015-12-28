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

    private City city;
    private static final String IMAGE = "image";
    private ImageView camImageView;
    private ProgressBar progressView;
    private WebcamInfoDownloader downloader;

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
            downloader = (WebcamInfoDownloader) getLastCustomNonConfigurationInstance();
        }
        if (downloader == null) {
            downloader = new WebcamInfoDownloader(this, city);
            downloader.execute();
        }
    }

    private static final String TAG = "CityCam";

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloader;
    }

    @Override
        protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (downloader.getProgress() == WebcamInfoDownloader.Progress.DONE || downloader.getProgress() == WebcamInfoDownloader.Progress.ERROR) {
                progressView.setVisibility(View.GONE);
                Bitmap img = (savedInstanceState.getParcelable(IMAGE));
                camImageView.setImageBitmap(img);
            }
            progressView.setVisibility(View.VISIBLE);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
        protected void onSaveInstanceState(Bundle outState) {
                Drawable img_to_bundle = camImageView.getDrawable();
                if (img_to_bundle != null) {
                        outState.putParcelable(IMAGE, ((BitmapDrawable) img_to_bundle).getBitmap());
                    }
                super.onSaveInstanceState(outState);
            }
}
