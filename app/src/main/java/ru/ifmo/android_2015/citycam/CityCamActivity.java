package ru.ifmo.android_2015.citycam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.Webcam;
import ru.ifmo.android_2015.citycam.webcams.Webcams;
import ru.ifmo.android_2015.citycam.utils.*;

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
    private ImageView camImageView;
    private ProgressBar progressView;

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
        }

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.

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
        // Текущий объект Activity, храним для обновления отображения
        private CityCamActivity activity;

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
                //activity.titleTextView.setText(state.titleResId);
                activity.progressView.setProgress(progress);
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
            try {
                URL nearbyUrl = Webcams.createNearbyUrl(city.latitude, city.longitude);
                HttpURLConnection urlConnection = (HttpURLConnection) nearbyUrl.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    List<Webcam> result = JSONHandler.readJsonStream(in);
                    Webcam sample = result != null ? result.get(0) : null;
                } catch (Exception e) {
                    Log.e("Connection error: ", e.getMessage());
                } finally
                {
                    urlConnection.disconnect();
                }
            }
            catch (MalformedURLException e) {
                Log.e("Malformed URL: ", e.getMessage());
            } catch (IOException e) {
                Log.e("IO exception: ", e.getMessage());
            }
            try {
                //downloadFile(appContext, this /*progressCallback*/);
                state = DownloadState.DONE;

            } catch (Exception e) {
                Log.e(TAG, "Error downloading file: " + e, e);
                state = DownloadState.ERROR;
            }
            return state;
        }

        // Метод ProgressCallback, вызывается в фоновом потоке из downloadFile
        @Override
        public void onProgressChanged(int progress) {
            publishProgress(progress);
        }

        // Метод AsyncTask, вызывается в UI потоке в результате вызова publishProgress
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
            }
            updateView();
        }

        static Bitmap downloadAndDecode(Webcam camera) {
            Bitmap result = null;
            try {
                URL imageUrl = new URL(camera.imageUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) imageUrl.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                result = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Decoding exception: ", e.getMessage());
            }
            return result;
        }
    }

    private static final String TAG = "CityCam";
}
