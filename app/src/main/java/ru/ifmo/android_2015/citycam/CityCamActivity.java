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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import ru.ifmo.android_2015.citycam.Utils.DownloadUtils;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcam;
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

    private DownloadImageTask downloadImageTask = null;

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

        progressView.setVisibility(View.VISIBLE);
        if (savedInstanceState != null){
            downloadImageTask = (DownloadImageTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadImageTask == null) {
            downloadImageTask = new DownloadImageTask(this);
            downloadImageTask.execute(city);
        } else {
            downloadImageTask.setActivity(this);
        }
        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (downloadImageTask.progress == DownloadImageTask.Progress.DONE){
            progressView.setVisibility(View.INVISIBLE);
            camImageView.setImageBitmap(downloadImageTask.getBitmap());
        } else {
            if (downloadImageTask.progress == DownloadImageTask.Progress.NOWEBCAM){
                progressView.setVisibility(View.INVISIBLE);
                camImageView.setImageResource(R.drawable.missing_webcam);
            }
        }
    }

    static class DownloadImageTask extends AsyncTask<City, Void, Boolean>{

        CityCamActivity activity;
        Bitmap bitmap = null;

        enum Progress{
            DOWNLOADING,
            DONE,
            NOWEBCAM
        }
        Progress progress = Progress.DOWNLOADING;

        public Bitmap getBitmap(){
            return bitmap;
        }

        public DownloadImageTask(CityCamActivity activity) {
            this.activity = activity;
        }

        public void setActivity(CityCamActivity activity) {
            this.activity = activity;
        }

        @Override
        protected Boolean doInBackground(City... params) {
            City city = params[params.length - 1];
            try {
                Webcam webcam = DownloadUtils.downloadWebcamInfo(Webcams.createNearbyUrl(city.latitude, city.longitude));
                if (webcam != null) {
                    bitmap = DownloadUtils.downloadBitmap(webcam.url);
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Couldn't create url for " + city.name);
            } catch (IOException e) {
                Log.e(TAG, "Couldn't open connection");
            }
            return bitmap != null;
        }

        @Override
        protected void onPostExecute(Boolean succesfulDownload) {
            super.onPostExecute(succesfulDownload);
            if (activity != null) {
                if (succesfulDownload) {
                    activity.camImageView.setImageBitmap(bitmap);
                    progress = Progress.DONE;
                } else {
                    activity.camImageView.setImageResource(R.drawable.missing_webcam);
                    progress = Progress.NOWEBCAM;
                }
                activity.progressView.setVisibility(View.INVISIBLE);
            }
        }
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if (bitmap != null) {
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
//            byte[] image = outputStream.toByteArray();
//            outState.putByteArray("Image", image);
//        }
//    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadImageTask;
    }

    private static final String TAG = "CityCam";
}