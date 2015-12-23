package ru.ifmo.android_2015.citycam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

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

    private City city;

    private TextView camTextView;
    private ImageView camImageView;
    private ProgressBar progressView;


    // Выполняющийся таск загрузки файла
    private DownloadFileTask downloadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        setContentView(R.layout.activity_city_cam);
        camTextView = (TextView) findViewById(R.id.textView);
        camImageView = (ImageView) findViewById(R.id.cam_image);
        progressView = (ProgressBar) findViewById(R.id.progress);
        progressView.setMax(100);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            downloadTask = (DownloadFileTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask == null) {
            downloadTask = new DownloadFileTask(this);
            downloadTask.execute();
        } else {
            downloadTask.attachActivity(this);
        }
    }

    public Object onRetainCustomNonConfigurationInstance() {
        return downloadTask;
    }

    //состояние загрузки DownloadFileTask
    enum DownloadState {
        DOWNLOADING(R.string.downloading),
        DONE(R.string.done),
        ERROR(R.string.error);

        final int titleResId;

        DownloadState(int titleResId) {
            this.titleResId = titleResId;
        }
    }

    //Скачивание xml файла и изображения в фоновом потоке
    static class DownloadFileTask extends AsyncTask<Void, Void, DownloadState> {

        private CityCamActivity activity;
        private DownloadState state;
        private String eMessage;
        private Bitmap webcamPhoto;
        Webcam mostPopularWebcam;

        DownloadFileTask(CityCamActivity activity) {
            this.activity = activity;
            this.state = DownloadState.DOWNLOADING;
            this.eMessage = "";
            this.webcamPhoto = null;
            this.mostPopularWebcam = null;
        }

        /**
         * Этот метод вызывается, когда новый объект Activity подключается к
         * данному таску после смены конфигурации.
         *
         * @param activity новый объект Activity
         */
        void attachActivity(CityCamActivity activity) {
            this.activity = activity;
            updateView();
        }

        void updateView() {
            if (activity != null) {
                if (state == DownloadState.DONE) {
                    activity.camImageView.setImageBitmap(webcamPhoto);
                    activity.progressView.setVisibility(View.GONE);

                    String text = "";
                    if (mostPopularWebcam.title.length() > 0) {
                        text += "TITLE: " + mostPopularWebcam.title + "\n";
                    }
                    if (mostPopularWebcam.user.length() > 0) {
                        text += "USER: " + mostPopularWebcam.user + "\n";
                    }
                    if (mostPopularWebcam.viewCount != -1) {
                        text += "VIEW COUNT: " + mostPopularWebcam.viewCount + "\n";
                    }
                    activity.camTextView.setText(text);
                }
                if (state == DownloadState.ERROR) {
                    activity.camImageView.setImageResource(R.drawable.error);
                    activity.camTextView.setText("ERROR\n" + eMessage);
                    activity.progressView.setVisibility(View.GONE);
                }
                if (state == DownloadState.DOWNLOADING) {
                    activity.camTextView.setText("DOWNLOADING");
                }
            }
        }

        /**
         * Вызывается в UI потоке из execute() до начала выполнения таска.
         */
        @Override
        protected void onPreExecute() {
            updateView();
        }

        @Override
        protected DownloadState doInBackground(Void... ignore) {
            URL url;

            try {
                url = Webcams.createNearbyUrl(activity.city.latitude, activity.city.longitude);
            } catch (Exception e) {
                Log.e(TAG, "Creating URL problem." + e.getMessage(), e);
                eMessage = "Creating URL problem.";
                return DownloadState.ERROR;
            }

            List<Webcam> w = downloadJSON(url);
            if (w == null) {
                return DownloadState.ERROR;
            }
            if (w.size() == 0) {
                eMessage = "There are no webcams here.";
                return DownloadState.ERROR;
            }

            mostPopularWebcam = w.get(0);
            for (int i = 1; i < w.size(); i++) {
                if (mostPopularWebcam.viewCount < w.get(i).viewCount) {
                    mostPopularWebcam = w.get(i);
                }
            }

            Log.d(TAG, "Gonna download bitmap: " + mostPopularWebcam.previewURL);
            webcamPhoto = downloadBitmap(mostPopularWebcam.previewURL);
            if (webcamPhoto == null) {
                return DownloadState.ERROR;
            }

            return DownloadState.DONE;
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

        @Override
        protected void onPostExecute(DownloadState state) {
            this.state = state;
            updateView();
        }

        List<Webcam> downloadJSON(URL url) {
            Log.d(TAG, "!!!!!!!!!!!!!!!!!Start downloading JSON: " + url.toString());

            HttpURLConnection urlConnection = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "Received HTTP response code: " + responseCode);
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Downloading information problem");
                    eMessage = "Downloading information problem.";
                    return null;
                }

                return JSONHandler.readInputStream(urlConnection.getInputStream());

            } catch (Exception e){
                Log.e(TAG, "Downloading problem" + e.getMessage(), e);
                eMessage = "Downloading problem.";
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }


        Bitmap downloadBitmap(String strUrl) {
            Log.d(TAG, "!!!!!!!!!!!!!!!!!Start downloading bitmap: " + strUrl);

            URL url;
            Bitmap res;

            try {
                url = new URL(strUrl);
            } catch (Exception e) {
                Log.e(TAG, "Creating image URL problem." + e.getMessage(), e);
                eMessage = "Creating image URL problem.";
                return null;
            }

            HttpURLConnection urlConnection = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "Received HTTP response code: " + responseCode);
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Downloading image problem");
                    eMessage = "Downloading image problem.";
                    return null;
                }

                res = BitmapFactory.decodeStream(urlConnection.getInputStream());
            } catch (Exception e){
                Log.e(TAG, "Downloading image problem" + e.getMessage(), e);
                eMessage = "Downloading image problem.";
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return res;
        }


    }

    private static final String TAG = "CityCam";
}
