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

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
    private ImageView camImageView;
    private ProgressBar progressView;
    private DownloadTask downloadTask;
    private Cam thisCam = null;
    private Bitmap camImg = null;
    private TextView textView;

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
        textView = (TextView)findViewById(R.id.textViewCamName);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);


        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.

        if (savedInstanceState != null) {
            // Пытаемся получить ранее запущенный таск
            downloadTask = (DownloadTask) getLastCustomNonConfigurationInstance();
            camImg = savedInstanceState.getParcelable("img");
        }
        if (downloadTask == null) {
            // Создаем новый таск, только если не было ранее запущенного таска
            downloadTask = new DownloadTask(this);
            downloadTask.execute();
        } else {
            // Передаем в ранее запущенный таск текущий объект Activity
            downloadTask.attachActivity(this);
        }

        if (camImg != null) {
            camImageView.setImageBitmap(camImg);
            progressView.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    @SuppressWarnings("deprecation")
    public Object onRetainCustomNonConfigurationInstance() {
        // Этот метод вызывается при смене конфигурации, когда текущий объект
        // Activity уничтожается. Объект, который мы вернем, не будет уничтожен,
        // и его можно будет использовать в новом объекте Activity
        return downloadTask;
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("img", camImg);
    }

    public class DownloadTask extends AsyncTask<Void, Void, Cam> {
        // Текущий объект Activity, храним для обновления отображения
        private CityCamActivity activity;
        private Boolean successful;

        DownloadTask(CityCamActivity activity) {
            this.activity = activity;
        }

        void attachActivity(CityCamActivity activity) {
            this.activity = activity;
            //updateView();
        }

        @Override
        protected void onPreExecute() {
            //  Log.d(TAG, "PreExecute ..");
        }

        @Override
        protected Cam doInBackground(Void... ignore) {
            Cam result = null;
            try {
                URL camURL = Webcams.createNearbyUrl(city.latitude, city.longitude);
                result = new CamParceUtils().readJSONStream(camURL);
                Log.d(TAG, "Cam image URL: " + result.preview_url);
                Log.d(TAG, "Parce successful");
            } catch (Exception e) {
                Log.e(TAG, "Error downloading file: " + e, e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Cam result) {
            // Log.d(TAG, "successful: " + successful);
            if (result != null) {
                Log.d(TAG, "Successful parse cam");
                thisCam = result;
                textView.setText(thisCam.title);
                DownloadImageTask downloadImageTask = new DownloadImageTask();
                downloadImageTask.execute(thisCam.preview_url);
            } else {
                progressView.setVisibility(View.INVISIBLE);
                textView.setText("Ошибка загрузки, попробуйте позже");
            }
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        public DownloadImageTask() {
        }
        protected Bitmap doInBackground(String... urls) {
            Log.d(TAG, "Start downloading picture...");
            String urldisplay = urls[0];
            Bitmap image = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", "Error downloading img");
                e.printStackTrace();
            }
            return image;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                camImg = result;
                camImageView.setImageBitmap(result);
                progressView.setVisibility(View.INVISIBLE);
            } else {
                progressView.setVisibility(View.INVISIBLE);
                textView.setText("Ошибка загрузки, попробуйте позже");
            }
        }
    }

    private static final String TAG = "CityCam";
}
