package ru.ifmo.android_2015.citycam;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import ru.ifmo.android_2015.citycam.GetWebcamImageAsyncTask.Progress;
import ru.ifmo.android_2015.citycam.model.City;

public class CityCamActivity extends AppCompatActivity {

    public static final String EXTRA_CITY = "city";
    private static final String TAG = "CityCam";
    private static final String TITLE = "title";
    private static final String LAST_UPDATED = "last_updated";
    private static final String RATING = "rating";
    private static final String IMAGE = "image";
    ProgressBar progressView;
    TextView titleView, lastUpdatedView;
    ImageView camImageView;
    RatingBar ratingView;
    private GetWebcamImageAsyncTask loadCam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        City city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        setContentView(R.layout.activity_city_cam);
        progressView = (ProgressBar) findViewById(R.id.progressBar);
        titleView = (TextView) findViewById(R.id.titleText);
        lastUpdatedView = (TextView) findViewById(R.id.lastUpdatedText);
        camImageView = (ImageView) findViewById(R.id.camImage);
        ratingView = (RatingBar) findViewById(R.id.ratingBar);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            loadCam = (GetWebcamImageAsyncTask) getLastCustomNonConfigurationInstance();
        }
        if (loadCam == null) {
            loadCam = new GetWebcamImageAsyncTask(this);
            loadCam.execute(city);
        } else {
            loadCam.attach(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return loadCam;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            titleView.setText(savedInstanceState.getString(TITLE));
            if (loadCam.getProgress() != Progress.Downloading)
                progressView.setVisibility(View.GONE);

            if (loadCam.getProgress() == Progress.LoadedImage || loadCam.getProgress() == Progress.LoadedData) {
                lastUpdatedView.setText(savedInstanceState.getString(LAST_UPDATED));
                ratingView.setRating(savedInstanceState.getFloat(RATING));
                lastUpdatedView.setVisibility(View.VISIBLE);
                ratingView.setVisibility(View.VISIBLE);
            }

            if (loadCam.getProgress() == Progress.LoadedImage || loadCam.getProgress() == Progress.Error) {
                Bitmap img = (savedInstanceState.getParcelable(IMAGE));
                camImageView.setImageBitmap(img);
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(TITLE, titleView.getText().toString());
        outState.putString(LAST_UPDATED, lastUpdatedView.getText().toString());
        outState.putFloat(RATING, ratingView.getRating());
        Drawable img_to_bundle = camImageView.getDrawable();
        if (img_to_bundle != null) {
            outState.putParcelable(IMAGE, ((BitmapDrawable) img_to_bundle).getBitmap());
        }
        super.onSaveInstanceState(outState);
    }
}
