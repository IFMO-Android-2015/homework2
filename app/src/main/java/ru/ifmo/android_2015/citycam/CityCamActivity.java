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
import android.widget.RatingBar;
import android.widget.TextView;

import ru.ifmo.android_2015.citycam.GetWebcamPreviewAsyncTask.Progress;
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
    private static final String TAG = "CityCam";
    private static final String TITLE_VIEW = "title_view";
    private static final String LASTUPD_VIEW = "lastupd_view";
    private static final String RATING_VIEW = "rating_view";
    private static final String BITMAP_VIEW = "bitmap_view";
    ProgressBar progressView;
    TextView titleView, lastUpdateView;
    ImageView camView;
    RatingBar ratingView;
    private GetWebcamPreviewAsyncTask loadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        City city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        setContentView(R.layout.activity_city_cam);
        progressView = (ProgressBar) findViewById(R.id.fetchBar);
        titleView = (TextView) findViewById(R.id.titleTextView);
        lastUpdateView = (TextView) findViewById(R.id.lastUpdateView);
        camView = (ImageView) findViewById(R.id.cam_image);
        ratingView = (RatingBar) findViewById(R.id.ratingBar);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle(city.name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.

        if (savedInstanceState != null) {
            loadTask = (GetWebcamPreviewAsyncTask) getLastCustomNonConfigurationInstance();
        }
        if (loadTask == null) {
            loadTask = new GetWebcamPreviewAsyncTask(this);
            loadTask.execute(city);
        } else {
            loadTask.attachActivity(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return loadTask;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            titleView.setText(savedInstanceState.getString(TITLE_VIEW));
            if (loadTask.getProgress() != Progress.Downloading)
                progressView.setVisibility(View.GONE);

            if (loadTask.getProgress() == Progress.FetchedBitmap || loadTask.getProgress() == Progress.FetchedData) {
                lastUpdateView.setText(savedInstanceState.getString(LASTUPD_VIEW));
                ratingView.setRating(savedInstanceState.getFloat(RATING_VIEW));
                lastUpdateView.setVisibility(View.VISIBLE);
                ratingView.setVisibility(View.VISIBLE);
            }

            if (loadTask.getProgress() == Progress.FetchedBitmap || loadTask.getProgress() == Progress.Error) {
                Bitmap b = (savedInstanceState.getParcelable(BITMAP_VIEW));
                camView.setImageBitmap(b);
            }

        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(TITLE_VIEW, titleView.getText().toString());
        outState.putString(LASTUPD_VIEW, lastUpdateView.getText().toString());
        outState.putFloat(RATING_VIEW, ratingView.getRating());
        Drawable d = camView.getDrawable();
        if (d != null)
            outState.putParcelable(BITMAP_VIEW, ((BitmapDrawable) d).getBitmap());
        super.onSaveInstanceState(outState);
    }
}
