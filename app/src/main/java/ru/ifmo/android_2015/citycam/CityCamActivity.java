package ru.ifmo.android_2015.citycam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.Webcam;
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

    private static City city;
    private DownloadFileTask downloadTask;
    private static ImageView camImageView;
    private static TextView cameraName;
    private static TextView cameraLocation;
    private static TextView cameraTime;
    private static ProgressBar progressView;

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
        cameraName = (TextView) findViewById(R.id.cam_name);
        cameraLocation = (TextView) findViewById(R.id.cam_location);
        cameraTime = (TextView) findViewById(R.id.cam_time);

        getSupportActionBar().setTitle(city.name);

        progressView.setMax(100);
        progressView.setVisibility(View.VISIBLE);


        if (savedInstanceState != null) {
            // Пытаемся получить ранее запущенный таск
            downloadTask = (DownloadFileTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask == null) {
            // Создаем новый таск, только если не было ранее запущенного таска
            downloadTask = new DownloadFileTask(this);
            downloadTask.execute();
        } else {
            // Передаем в ранее запущенный таск текущий объект Activity
            downloadTask.attachActivity(this);
            downloadTask.setCameraData();
        }

    }

    @SuppressWarnings("deprecation")
    public Object onRetainCustomNonConfigurationInstance() {
        // Этот метод вызывается при смене конфигурации, когда текущий объект
        // Activity уничтожается. Объект, который мы вернем, не будет уничтожен,
        // и его можно будет использовать в новом объекте Activity
        return downloadTask;
    }

    /**
     * Состояние загрузки в DownloadFileTask
     */
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

    /**
     * Таск, выполняющий скачивание файла в фоновом потоке.
     */
    static class DownloadFileTask extends AsyncTask<Void, Integer, DownloadState>
            implements ProgressCallback {

        // Context приложения (Не Activity!) для доступа к файлам
        private Context appContext;
        private Bitmap image;
        // Текущий объект Activity, храним для обновления отображения
        private CityCamActivity activity;
        private Webcam selected;
        // Текущее состояние загрузки
        private DownloadState state = DownloadState.DOWNLOADING;
        // Прогресс загрузки от 0 до 100
        private int progress;

        DownloadFileTask(CityCamActivity activity) {
            this.appContext = activity.getApplicationContext();
            this.activity = activity;
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

        /**
         * Вызываем на UI потоке для обновления отображения прогресса и
         * состояния в текущей активности.
         */
        void updateView() {
            if (activity != null) {
                activity.progressView.setProgress(progress);
                if (progress == 100)
                    progressView.setVisibility(View.GONE);
            }
        }

        /**
         * Вызывается в UI потоке из execute() до начала выполнения таска.
         */
        @Override
        protected void onPreExecute() {
            updateView();
        }

            /**
         * Скачивание файла в фоновом потоке. Возвращает результат:
         *      0 -- если файл успешно скачался
         *      1 -- если произошла ошибка
         */
        @Override
        @SuppressWarnings("unchecked")
        protected DownloadState doInBackground(Void... ignore) {

            //Debug.waitForDebugger();
            selected = null;
            try {
                URL nearbyUrl = Webcams.createNearbyUrl(city.latitude, city.longitude);
                HttpURLConnection urlConnection = (HttpURLConnection) nearbyUrl.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    List<Webcam> result = JSONHandler.readJsonStream(in);
                    Random random = new Random();
                    if (result != null && result.size() != 0)
                        selected = result.get(random.nextInt(result.size() - 1));
                    else {
                        state = DownloadState.ERROR;
                        return state;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Connection error: " + e.getMessage());
                    state = DownloadState.ERROR;
                } finally {
                    urlConnection.disconnect();
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Malformed URL: " + e.getMessage());
                state = DownloadState.ERROR;
            } catch (IOException e) {
                Log.e(TAG, "IO exception: " + e.getMessage());
                state = DownloadState.ERROR;
            }
            try {
                image = downloadAndDecode(); //, appContext, this);
                state = DownloadState.DONE;
            } catch (NullPointerException e) {
                Log.e(TAG, "No camera found: " + e.getMessage());
                state = DownloadState.ERROR;
            }
            return state;
        }

        @Override
        public void onProgressChanged(int progress) {
            publishProgress(progress);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values.length > 0) {
                int progress = values[values.length - 1];
                this.progress = progress;
                updateView();
            }
        }

        @Override
        protected void onPostExecute(DownloadState state) {
            // Проверяем код, который вернул doInBackground и показываем текст в зависимости
            // от результата
            this.state = state;
            if (state == DownloadState.DONE) {
                progress = 100;
                setCameraData();
            }
            progressView.setVisibility(View.GONE);
            if (state == DownloadState.ERROR || selected == null) {
                Toast.makeText(appContext, "Failed to load image from selected city", Toast.LENGTH_SHORT).show();
            }
            updateView();
        }

        Bitmap downloadAndDecode() {

            Bitmap result = null;
            try {
                URL imageUrl = new URL(selected.imageUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) imageUrl.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                result = BitmapFactory.decodeStream(in);
            }
            catch (MalformedURLException e) {
                Log.e(TAG, "Malformed URL: " + e.getMessage());
                state = DownloadState.ERROR;
            }
            catch (IOException e) {
                Log.e(TAG, "Error while decoding stream: " + e.getMessage());
            }
            return result;
        }

        void setCameraData() {
            if (image != null) {
                camImageView.setImageBitmap(image);
                cameraName.setText(selected.title);
                cameraLocation.setText(selected.location);
                Calendar date = Calendar.getInstance();
                date.setTimeInMillis(selected.unixTime * 1000);
                cameraTime.setText(dateFormat(date));
            }
        }

        String dateFormat(Calendar date) {
            return date.get(Calendar.DAY_OF_MONTH) + "."
                    + date.get(Calendar.MONTH) + "."
                    + date.get(Calendar.YEAR) + "\t"
                    + String.format("%02d", date.get(Calendar.HOUR_OF_DAY)) + ":"
                    + String.format("%02d", date.get(Calendar.MINUTE));
        }
    }


    private static final String TAG = "CityCam";
}
