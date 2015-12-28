package ru.ifmo.android_2015.citycam;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.util.Loader;

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
    private DownloadCityTask downloadCityTask;
    private TextView titleTextView;

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
        titleTextView = (TextView) findViewById(R.id.title);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
        if (savedInstanceState != null) {
            downloadCityTask = (DownloadCityTask) getLastCustomNonConfigurationInstance();
        }

        if (downloadCityTask != null) {
            downloadCityTask.attachActivity(this);
        } else {
            downloadCityTask = new DownloadCityTask(this, city);
            downloadCityTask.execute();
        }
    }

    public Object onRetainCustomNonConfigurationInstance() {
        return downloadCityTask;
    }

    enum DownloadState {
        DOWNLOADING(R.string.downloading),
        DONE(R.string.done),
        ERROR(R.string.error);

        final int titleRId;
        DownloadState(int titleRId) {
            this.titleRId = titleRId;
        }
    }


    private static final String TAG = "CityCam";

    static class DownloadCityTask extends AsyncTask<Void, Integer, DownloadState> {
        private City.Data cityData;
        private City city;
        private DownloadState downloadState = DownloadState.DOWNLOADING;
        private CityCamActivity activity;

        private DownloadCityTask(final CityCamActivity activity, final City city) {
            this.activity = activity;
            this.city = city;
        }

        void updateActivity() {
            if (activity != null) {
                if (downloadState == DownloadState.DONE) {
                    activity.camImageView.setImageBitmap(cityData.image);
                    activity.titleTextView.setText(cityData.title);
                } else {
                    activity.titleTextView.setText(R.string.no_camera);
                }
                activity.progressView.setVisibility(View.GONE);
            }
        }

        private void attachActivity(final CityCamActivity activity) {
            this.activity = activity;
            updateActivity();
        }

        @Override
        protected DownloadState doInBackground(Void... params) {
            try {
                cityData = Loader.getCityData(city);
                cityData.image = Loader.getBitmap(cityData.url_of_preview);
                downloadState = DownloadState.DONE;
            } catch (Exception e) {
                downloadState = DownloadState.ERROR;
            }
            return downloadState;
        }

        @Override
        protected void onPostExecute(DownloadState downloadState) {
            super.onPostExecute(downloadState);
            updateActivity();
        }
    }
}
