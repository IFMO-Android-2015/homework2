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
import ru.ifmo.android_2015.citycam.model.CityData;
import ru.ifmo.android_2015.citycam.util.BitmapLoader;
import ru.ifmo.android_2015.citycam.util.JsonLoader;

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
    private TextView titleTextView;
    private ProgressBar progressView;
    private DownloadTask downloadTask;

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
        titleTextView = (TextView) findViewById(R.id.title);
        progressView = (ProgressBar) findViewById(R.id.progress);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
        if (savedInstanceState != null) {
            downloadTask = (DownloadTask) getLastCustomNonConfigurationInstance();
        }

        if (downloadTask != null) {
            downloadTask.attachActivity(this);
        } else {
            downloadTask = new DownloadTask(this, city);
            downloadTask.execute();
        }
    }

    public Object onRetainCustomNonConfigurationInstance() {
        return downloadTask;
    }

    private static final String TAG = "CityCam";

    enum DownloadState {
        DOWNLOADING(R.string.downloading),
        DONE(R.string.done),
        ERROR(R.string.error);

        // ID строкового ресурса для заголовка окна прогресса
        final int titleResId;

        DownloadState(int titleResId) {
            this.titleResId = titleResId;
        }
    }

    static class DownloadTask extends AsyncTask<Void, Integer, DownloadState> {

        private CityCamActivity activity;
        private DownloadState state = DownloadState.DOWNLOADING;
        private City city;
        private CityData cityData;

        private DownloadTask(final CityCamActivity activity, final City city) {
            this.activity = activity;
            this.city = city;
        }

        void updateView() {
            if (activity != null) {
                if (state == DownloadState.DONE) {
                    activity.camImageView.setImageBitmap(cityData.preview_image);
                    activity.titleTextView.setText(cityData.title);
                } else {
                    activity.titleTextView.setText(R.string.no_camera);
                }
                activity.progressView.setVisibility(View.GONE);
            }
        }

        private void attachActivity(final CityCamActivity activity) {
            this.activity = activity;
            updateView();
        }

        @Override
        protected DownloadState doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");
            try {

                cityData = JsonLoader.getCityData(city);
                cityData.preview_image = BitmapLoader.getBitmap(cityData.preview_url);

                state = DownloadState.DONE;
            } catch (Exception e) {
                state = DownloadState.ERROR;
            }

            return state;
        }

        @Override
        protected void onPostExecute(DownloadState state) {
            updateView();
        }
    }
}