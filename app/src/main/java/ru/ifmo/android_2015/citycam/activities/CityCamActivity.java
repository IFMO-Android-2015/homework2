package ru.ifmo.android_2015.citycam.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.Locale;

import ru.ifmo.android_2015.citycam.R;
import ru.ifmo.android_2015.citycam.WebCamLoaderTask;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.WebCam;

/**
 * Экран, показывающий веб-камеру одного выбранного города.
 * Выбранный город передается в extra параметрах.
 */
public class CityCamActivity extends AppCompatActivity {

    private static final String TAG = "CityCam";

    /**
     * Обязательный extra параметр - объект City, камеру которого надо показать.
     */
    public static final String EXTRA_CITY = "city";
    private City city;
    private WebCamLoaderTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        setContentView(R.layout.activity_city_cam);
        getSupportActionBar().setTitle(city.name);

        if (savedInstanceState != null) {
            task = (WebCamLoaderTask)getLastCustomNonConfigurationInstance();
        }
        if (task == null) {
            task = new WebCamLoaderTask(this);
            task.execute(city);
        } else {
            task.attachActivity(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return task;
    }
    
    public void updateUI(WebCamLoaderTask.DownloadResult result, @Nullable WebCam webCam, 
                         @Nullable Bitmap bitmap) {
        ProgressBar progressView = (ProgressBar)findViewById(R.id.progress);
        progressView.setVisibility(View.GONE);

        if (result == WebCamLoaderTask.DownloadResult.SUCCESS) {

            ImageView camImageView = (ImageView)findViewById(R.id.cam_image);
            camImageView.setImageBitmap(bitmap);

            TextView lastUpdate = (TextView)findViewById(R.id.last_update);
            PrettyTime p = new PrettyTime(new Locale("ru"));
            String prettifiedTime = p.format(new Date(webCam.getLastUpdate() * 1000));
            String lastUpdateValue = getString(R.string.property_last_update) + prettifiedTime;
            lastUpdate.setText(lastUpdateValue);

            TextView viewCount = (TextView)findViewById(R.id.view_count);
            viewCount.setText("Просмотрено: "+String.valueOf(webCam.getViewCount())+" раз");

            TextView title = (TextView)findViewById(R.id.title);
            title.setText(webCam.getTitle());

        } else if (result == WebCamLoaderTask.DownloadResult.ERROR) {
            ImageView failedImageView = (ImageView)findViewById(R.id.failed_image);
            failedImageView.setVisibility(View.VISIBLE);

            TextView title = (TextView)findViewById(R.id.failed_text);
            title.setText("Не удалось загрузить изображение");

        } else if (result == WebCamLoaderTask.DownloadResult.NO_INTERNET) {
            Toast.makeText(this, getString(R.string.message_check_internet_connection),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
