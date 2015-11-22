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

public class CityCamActivity extends AppCompatActivity {

    public static final String EXTRA_CITY = "city";

    private City city;

    private ImageView camImageView;
    private ProgressBar progressView;
    private TextView camTitle,lat, lon;

    private LoadingAsyncTask downloadTask;

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
        camTitle = (TextView) findViewById(R.id.cam_title);
        lat = (TextView) findViewById(R.id.latitude);
        lon = (TextView) findViewById(R.id.longitude);
        getSupportActionBar().setTitle(city.name);
        progressView.setVisibility(View.VISIBLE);
        if (savedInstanceState != null) {
            downloadTask = (LoadingAsyncTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask == null) {
            downloadTask = new LoadingAsyncTask(this);
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
        bundle.putString("TITLE", camTitle.getText().toString());
        bundle.putString("LAT", lat.getText().toString());
        bundle.putString("LON", lon.getText().toString());
        Drawable temp = camImageView.getDrawable();
        if (temp != null) {
            bundle.putParcelable("IMAGE", ((BitmapDrawable) temp).getBitmap());
        }
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        if (bundle != null) {
            camTitle.setText(bundle.getString("TITLE"));
            lat.setText(bundle.getString("LAT"));
            lon.setText(bundle.getString("LON"));
            if (downloadTask.getState() == LoadingAsyncTask.DownloadState.DOWNLOADING) {
                progressView.setVisibility(View.VISIBLE);
            } else {
                Bitmap bitmap = bundle.getParcelable("IMAGE");
                camImageView.setImageBitmap(bitmap);
                progressView.setVisibility(View.INVISIBLE);
            }
        }
        super.onRestoreInstanceState(bundle);
    }

    private static final String TAG = "CityCam";
}
