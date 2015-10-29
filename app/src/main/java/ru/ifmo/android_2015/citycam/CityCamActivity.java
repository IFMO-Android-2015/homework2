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
    private TextView updTime, avgRating, ratingsCount, viewCount;
    private GetWebCamData getWebCamData;
    private final String TAG = "CityCam";
    private final static String SaveKey[] = {"LastUPD",
                                             "RatingCount",
                                             "AverageRating",
                                             "ViewCount",
                                             "Image"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            finish();
        }

        setContentView(R.layout.activity_city_cam);
        camImageView = (ImageView) findViewById(R.id.cam_image);
        progressView = (ProgressBar) findViewById(R.id.progress);
        updTime = (TextView) findViewById(R.id.last_update);
        avgRating = (TextView) findViewById(R.id.AverageRating);
        ratingsCount = (TextView) findViewById(R.id.RatingsCount);
        viewCount = (TextView) findViewById(R.id.ViewCount);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.

        if (savedInstanceState == null) {
            Log.w(TAG, "Start AsyncTask");
            getWebCamData = new GetWebCamData(this);
            getWebCamData.execute(city);
        } else {
            getWebCamData = (GetWebCamData) getLastCustomNonConfigurationInstance();
            getWebCamData.attachActivity(this);
        }

    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return getWebCamData;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (getWebCamData.progress == "DownloadFinished") {
            Drawable pic = camImageView.getDrawable();
            outState.putParcelable(SaveKey[4], ((BitmapDrawable) pic).getBitmap());
        }
        if (getWebCamData.progress != "ERROR") {
            outState.putString(SaveKey[3], viewCount.getText().toString());
            outState.putString(SaveKey[2], avgRating.getText().toString());
            outState.putString(SaveKey[1], ratingsCount.getText().toString());
            outState.putString(SaveKey[0], updTime.getText().toString());
        }
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (getWebCamData.progress == "PictureDownloading"
                    || getWebCamData.progress == "DownloadFinished") {
                updTime.setText(savedInstanceState.getString(SaveKey[0]));
                ratingsCount.setText(savedInstanceState.getString(SaveKey[1]));
                avgRating.setText(savedInstanceState.getString(SaveKey[2]));
                viewCount.setText(savedInstanceState.getString(SaveKey[3]));
                updTime.setVisibility(View.VISIBLE);
                ratingsCount.setVisibility(View.VISIBLE);
                avgRating.setVisibility(View.VISIBLE);
                viewCount.setVisibility(View.VISIBLE);
            }
            if (getWebCamData.progress == "DownloadFinished") {
                Bitmap pic = savedInstanceState.getParcelable(SaveKey[4]);
                camImageView.setImageBitmap(pic);
                progressView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
