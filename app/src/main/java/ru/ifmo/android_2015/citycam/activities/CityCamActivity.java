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

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.Locale;

import ru.ifmo.android_2015.citycam.DownloadTask;
import ru.ifmo.android_2015.citycam.R;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.WebCam;

/**
 * Экран, показывающий веб-камеру одного выбранного города.
 * Выбранный город передается в extra параметрах.
 */
public class CityCamActivity extends AppCompatActivity {

    public static final String TAG = "CityCam";
    public static final String EXTRA_CITY = "city";

    /* stores an instance of the current DownloadTask */
    private DownloadTask task;

    private ProgressBar progressBar;
    private ImageView camImage;
    private TextView lastUpdate;
    private TextView viewCount;
    private TextView title;
    private ImageView failedImage;
    private TextView failedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        City city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        setContentView(R.layout.activity_city_cam);
        getSupportActionBar().setTitle(city.name);

        if (savedInstanceState != null) {
            task = (DownloadTask)getLastCustomNonConfigurationInstance();
        }

        if (task == null) {
            task = new DownloadTask(this);
            task.execute(city);
        } else {
            task.attachActivity(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return task;
    }
    
    public void updateUI(DownloadTask.Result result,
                         @Nullable WebCam webCam, @Nullable Bitmap bitmap) {

        progressBar = (ProgressBar)findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);

        if (result == DownloadTask.Result.SUCCESS) {

            camImage = (ImageView)findViewById(R.id.cam_image);
            camImage.setImageBitmap(bitmap);

            lastUpdate = (TextView)findViewById(R.id.last_update);
            PrettyTime p = new PrettyTime(new Locale("ru"));
            Date date = new Date(webCam.getLastUpdate() * 1000);
            String prettifiedTime = p.format(date);
            String lastUpdateValue = getString(R.string.last_update) + prettifiedTime;
            lastUpdate.setText(lastUpdateValue);

            viewCount = (TextView)findViewById(R.id.view_count);
            viewCount.setText(String.format(getResources().getString(R.string.view_count),
                    webCam.getViewCount()));

            title = (TextView)findViewById(R.id.title);
            title.setText(webCam.getTitle());

        } else if (result == DownloadTask.Result.ERROR) {

            failedImage = (ImageView)findViewById(R.id.failed_image);
            failedImage.setVisibility(View.VISIBLE);

            failedText = (TextView)findViewById(R.id.failed_text);
            failedText.setText(R.string.cant_download_image);

        }
    }
}
