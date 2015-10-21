package ru.ifmo.android_2015.citycam;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import ru.ifmo.android_2015.citycam.download.ProgressCallback;
import ru.ifmo.android_2015.citycam.file.CreateFile;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.reader.Reader;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

import static android.graphics.BitmapFactory.decodeFile;

/**
 * Экран, показывающий веб-камеру одного выбранного города.
 * Выбранный город передается в extra параметрах.
 */
public class CityCamActivity extends AppCompatActivity {

    /**
     * Обязательный extra параметр - объект City, камеру которого надо показать.
     */
    public static final String EXTRA_CITY = "city";

    private ProgressBar progressBarView;
    private ImageView camImageView;
    private Button left, right;
    private TextView cam;
    private DownloadFileTask downloadTask;
    private City city;
    private int current_cam;
    private long all_cam;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_cam);

        progressBarView = (ProgressBar) findViewById(R.id.progress_bar);
        camImageView = (ImageView) findViewById(R.id.cam_image);
        cam = (TextView) findViewById(R.id.cam);
        left = (Button) findViewById(R.id.left_button);
        right = (Button) findViewById(R.id.right_button);

        current_cam = 1;
        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        getSupportActionBar().setTitle(city.name);
        left.setVisibility(View.INVISIBLE);
        right.setVisibility(View.INVISIBLE);
        cam.setVisibility(View.INVISIBLE);
        progressBarView.setVisibility(View.VISIBLE);
        progressBarView.setMax(100);

        if (savedInstanceState != null) {
            // Пытаемся получить ранее запущенный таск
            downloadTask = (DownloadFileTask) getLastNonConfigurationInstance();
        }
        if (downloadTask == null) {
            try {
                URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);
                // Создаем новый таск, только если не было ранее запущенного таска
                downloadTask = new DownloadFileTask(this, url);
                downloadTask.execute();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            // Передаем в ранее запущенный таск текущий объект Activity
            downloadTask.attachActivity(this);
        }
    }

    public void rightClick(View view) {
        if (current_cam < all_cam) {
            current_cam++;
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.INVISIBLE);
            cam.setVisibility(View.INVISIBLE);
        }
    }

    public void leftClick(View view) {
        if (current_cam > 1) {
            current_cam--;
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.INVISIBLE);
            cam.setVisibility(View.INVISIBLE);
        }
    }

    public class DownloadFileTask extends AsyncTask<Void, Integer, Long>
            implements ProgressCallback {

        private Context context;
        private CityCamActivity activity;
        private File destFile;
        private URL url;

        private int progress;

        public DownloadFileTask(CityCamActivity activity, URL url) {
            this.url = url;
            this.context = activity.getApplicationContext();
            this.activity = activity;
        }

        private void updateView() {
            if (activity != null)
                activity.progressBarView.setProgress(progress);
        }


        public void attachActivity(CityCamActivity activity) {
            this.activity = activity;
            updateView();
        }

        @Override
        protected Long doInBackground(Void... params) {
            long count = 0;
            try {
                return downloadFile(url, context, this);
            } catch (IOException e) {
                e.printStackTrace();
                return count;
            }
        }

        protected void onProgressUpdate(Integer... values) {
            this.progress = values[values.length - 1];
            updateView();
        }

        @Override
        public void onProgressChanged(int progress) {
            publishProgress(progress);
        }

        @Override
        public void onPostExecute(Long resultCode) {
            // Этот метод выполняется в UI потоке
            // Параметр resultCode -- это результат doInBackground
            activity.progressBarView.setVisibility(View.INVISIBLE);
            activity.left.setVisibility(View.VISIBLE);
            activity.right.setVisibility(View.VISIBLE);
            activity.cam.setVisibility(View.VISIBLE);
            all_cam = resultCode;
            if (resultCode == 0)
                activity.cam.setText("Camera not found");
            else
                activity.cam.setText(current_cam + " of " + resultCode);
            Bitmap bit = decodeFile(destFile.getPath());
            activity.camImageView.setImageBitmap(bit);
        }

        private Long downloadFile(URL url, Context context,
                                  ProgressCallback progressCallback) throws IOException {
            destFile = CreateFile.createTempExternalFile(context, city.name, current_cam, ".jpg");
            return Reader.downloadJson(url, destFile, current_cam, progressCallback);
        }
    }

    private static final String TAG = "CityCam";
}
