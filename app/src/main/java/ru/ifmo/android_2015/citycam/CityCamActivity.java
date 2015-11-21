package ru.ifmo.android_2015.citycam;

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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.citycam.model.Cam;
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
    private DownloadFileTask downloadFileTask;
    private ImageView camImageView;
    private ProgressBar progressView;
    private TextView info;


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
        info = (TextView) findViewById(R.id.info);

        getSupportActionBar().setTitle(city.name);
        info.setText("Загрузка…");
        progressView.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            downloadFileTask = (DownloadFileTask) getLastCustomNonConfigurationInstance();
        }
        if (savedInstanceState == null) {
            downloadFileTask = new DownloadFileTask(this);
            downloadFileTask.execute();
        } else {
            downloadFileTask.attachActivity(this);
        }
        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadFileTask;
    }

    class DownloadFileTask extends AsyncTask<Void, Void, Void> {
        public Bitmap image = null;
        public Cam cam = null;
        private CityCamActivity activity = null;
        private Boolean res = false;


        @Override
        protected Void doInBackground(Void... params) {
            // Этот метод выполняется в фоновом потоке
            try {
                downloadFile();
            } catch (Exception e) {
                Log.e(TAG, "" + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            // Этот метод выполняется в UI потоке
            // Параметр res -- это результат doInBackground
            if (res) {
                activity.camImageView.setImageBitmap(image);
                activity.info.setText(cam.getName());
            } else {
                activity.info.setText("Камера отсутствует");

            }
            activity.progressView.setVisibility(View.INVISIBLE);
        }

        public DownloadFileTask(CityCamActivity activity) {
            this.activity = activity;
        }

        public void attachActivity(CityCamActivity activity) {
            this.activity = activity;
            onPostExecute(null);
        }

         void downloadFile() throws IOException {
             URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);
             HttpURLConnection connect = null;
             try {
                 res = false;
                 connect = (HttpURLConnection) url.openConnection();
                 InputStream input = new BufferedInputStream(connect.getInputStream());
                 Parse resParse = new Parse();
                 cam = resParse.parseJson(input);
                 HttpURLConnection connection = (HttpURLConnection) (new URL(cam.getPreview_url())).openConnection();
                 InputStream is = connection.getInputStream();
                 image = BitmapFactory.decodeStream(is);
                 if (image != null) {
                     res = true;
                     input.close();
                     is.close();
                 }
             } finally {
                 if (connect != null)
                     connect.disconnect();
             }
        }
    }

    private static final String TAG = "CityCam";

}
