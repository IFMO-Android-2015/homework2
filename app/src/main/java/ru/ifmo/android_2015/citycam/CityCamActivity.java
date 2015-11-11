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

    public City city;

    private ImageView camImageView = null;
    private DownloadFileTask downloadFileTask = null;
    private ProgressBar progressView;
    private TextView title;



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
        title = (TextView) findViewById(R.id.title);
        getSupportActionBar().setTitle(city.name);
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


    // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
    // в выбранном городе.


    class DownloadFileTask extends AsyncTask<Void, Void, Void> {
        public Bitmap image = null;
        public Webcam webcam = null;
        private CityCamActivity activity = null;
        private Boolean succes = false;

        @Override
        protected Void doInBackground(Void... params) {
            try {

                InputStream in = null;
                downloadFile(in);
                in.close();
            } catch (Exception e) {

                Log.e(TAG, "" + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            if (succes != false) {
                activity.progressView.setVisibility(View.INVISIBLE);
                activity.camImageView.setImageBitmap(image);
                activity.title.setText(webcam.getName());
            } else {
                activity.title.setText("Ошибка");
            }

        }

        public DownloadFileTask(CityCamActivity activity) {
            this.activity = activity;
        }

        public void attachActivity(CityCamActivity activity) {
            this.activity = activity;
            onPostExecute(null);
        }

        void downloadFile(InputStream in) throws IOException {
            URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);


            HttpURLConnection conn = null;
            try {
                succes = false;
                conn = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(conn.getInputStream());
                WebcamParse webcamParse = new WebcamParse();
                webcam = webcamParse.parseJson(in);
                HttpURLConnection connection = (HttpURLConnection) (new URL(webcam.getPreview_url())).openConnection();
                InputStream is = connection.getInputStream();
                image = BitmapFactory.decodeStream(is);
                if (image != null) {
                    succes = true;
                }
                if (connection != null) {
                    connection.disconnect();
                }
                if (is != null) {
                    is.close();
                }


            } finally {
                if (conn != null)
                    conn.disconnect();

            }
        }


    }
    private static final String TAG = "CityCam";
}







