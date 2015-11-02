package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;

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
    public String BitmapKey;

    City city;

    ImageView camImageView;
    TextView info;
    ProgressBar progressView;
    private WebCamTask currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }
        if (savedInstanceState == null) {
            BitmapKey = null;
        }
        setContentView(R.layout.activity_city_cam);
        info = (TextView) findViewById(R.id.info);
        info.setVisibility(View.INVISIBLE);
        camImageView = (ImageView) findViewById(R.id.cam_image);
        progressView = (ProgressBar) findViewById(R.id.progress);

        getSupportActionBar().setTitle(city.name);

        if (BitmapKey == null) {
            progressView.setVisibility(View.VISIBLE);
            if (savedInstanceState != null) {
                currentTask = (WebCamTask) getLastCustomNonConfigurationInstance();
            }
            if (currentTask == null) {
                Log.d(TAG, "starting new download");
                currentTask = new WebCamTask(this);
                currentTask.execute();
            } else {
                currentTask.attachActivity(this);
            }
        } else if (savedInstanceState != null) {
            byte[] savedImage = savedInstanceState.getByteArray(BitmapKey);
            camImageView.setImageBitmap(BitmapFactory.decodeByteArray(savedImage, 0, savedImage.length));
            progressView.setVisibility(View.INVISIBLE);

        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public Object onRetainCustomNonConfigurationInstance() {
        return currentTask;
    }


    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (BitmapKey != null) {
            Log.d(TAG, "saving picture");
            Bitmap b = ((BitmapDrawable)camImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream(b.getWidth() * b.getHeight());
            b.compress(Bitmap.CompressFormat.PNG, 100, stream);
            bundle.putByteArray(BitmapKey, stream.toByteArray());
        }
    }

    private static final String TAG = "CityCam";
}
