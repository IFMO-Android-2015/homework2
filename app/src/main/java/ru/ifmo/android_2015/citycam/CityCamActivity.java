package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.Util;
import ru.ifmo.android_2015.citycam.model.WebcamNotFoundException;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Экран, показывающий веб-камеру одного выбранного города.
 * Выбранный город передается в extra параметрах.
 */
public class CityCamActivity extends AppCompatActivity {

    /**
     * Обязательный extra парамеhтр - объект City, камеру которого надо показать.
     */
    public static final String EXTRA_CITY = "city";

    private City city;
    private Bitmap picture;
    private Util.WebcamInfo info;
    private Integer state;
    private DownloadTask downloadTask;

    private ImageView camImageView;
    private ProgressBar progressView;
    private LinearLayout infoLayout;
    private LinearLayout errorLayout;

    private TextView countryInfo;
    private TextView cityInfo;
    private TextView viewsInfo;
    private TextView ratingInfo;
    private TextView errorInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_cam);
        camImageView = (ImageView) findViewById(R.id.cam_image);
        progressView = (ProgressBar) findViewById(R.id.progress);
        infoLayout = (LinearLayout) findViewById(R.id.infoLayout);
        errorLayout = (LinearLayout) findViewById(R.id.errorLayout);

        countryInfo = (TextView) findViewById(R.id.countryName);
        cityInfo = (TextView) findViewById(R.id.cityName);
        viewsInfo = (TextView) findViewById(R.id.viewsCount);
        ratingInfo = (TextView) findViewById(R.id.ratingLabel);
        errorInfo = (TextView) findViewById(R.id.errorLabel);


        if (savedInstanceState != null) {
            city = savedInstanceState.getParcelable("city");
            picture = savedInstanceState.getParcelable("picture");
            state = savedInstanceState.getInt("state");
            info = savedInstanceState.getParcelable("info");
        } else {
            city = getIntent().getParcelableExtra(EXTRA_CITY);
            state = Result.PROGRESS;
        }
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        getSupportActionBar().setTitle(city.name);
        progressView.setVisibility(View.INVISIBLE);
        infoLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);

        switch (state) {
            case Result.OK:
                setActivityContent(this, info);
                break;
            case Result.NOCAM:
                setWebcamNotFoundContent(this);
                break;
            default:
                progressView.setVisibility(View.VISIBLE);

                if (savedInstanceState != null) {
                    downloadTask = (DownloadTask) getLastCustomNonConfigurationInstance();
                }
                if (downloadTask == null) {
                    downloadTask = new DownloadTask(this);
                    downloadTask.execute(city);
                } else {
                    downloadTask.attachActivity(this);
                }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("picture", picture);
        outState.putParcelable("city", city);
        outState.putInt("state", state);
        outState.putParcelable("info", info);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadTask;
    }

    private class Result {
        public static final int PROGRESS = 0;
        public static final int OK = 1;
        public static final int NOCAM = 2;
        public static final int ERROR = 3;
    }

    private void setActivityContent(CityCamActivity activity, Util.WebcamInfo info) {
        activity.camImageView.setImageBitmap(activity.picture);
        activity.infoLayout.setVisibility(View.VISIBLE);
        activity.countryInfo.setText(info.country);
        activity.cityInfo.setText(info.city);
        activity.viewsInfo.setText(Integer.toString(info.viewCount));
        if (info.rating > 0) {
            activity.ratingInfo.setText(new DecimalFormat("#.##").format(info.rating));
        } else {
            activity.ratingInfo.setText("Нет рейтинга");
        }
    }

    private void setWebcamNotFoundContent(CityCamActivity activity) {
        activity.errorLayout.setVisibility(View.VISIBLE);
        activity.errorInfo.setText("Вебкамеры не найдены");
    }

    private static class DownloadTask extends AsyncTask<City, Void, Integer> {
        private CityCamActivity activity;
        private Util.WebcamInfo info;
        private Bitmap picture;

        public DownloadTask(CityCamActivity activity) {
            this.activity = activity;
        }

        public void attachActivity(CityCamActivity activity) {
            this.activity = activity;
        }

        @Override
        protected Integer doInBackground(City... cities) {
            Log.i(TAG, "Task started");
            City city = cities[0];

            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) Webcams
                        .createNearbyUrl(city.latitude, city.longitude)
                        .openConnection();

                info = Util.parseRespond(conn);
                picture = Util.downloadBitmap(info.pictureUrl);
                return Result.OK;
            } catch (WebcamNotFoundException e) {
                return Result.NOCAM;
            } catch (IOException e) {
                Log.e("!", "Some error: " + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return Result.ERROR;
        }

        @Override
        protected void onPostExecute(Integer res) {
            super.onPostExecute(res);
            activity.progressView.setVisibility(View.GONE);
            activity.state = res;
            switch (res) {
                case Result.OK:
                    activity.picture = picture;
                    activity.info = info;
                    activity.setActivityContent(activity, info);
                    break;
                case Result.NOCAM:
                    activity.setWebcamNotFoundContent(activity);
                    break;
                case Result.ERROR:
                    activity.errorLayout.setVisibility(View.VISIBLE);
                    activity.errorInfo.setText("Данные не получены. \nПроверьте подключение к сети");
                    break;
            }
        }
    }

    private static final String TAG = "CityCam";
}
