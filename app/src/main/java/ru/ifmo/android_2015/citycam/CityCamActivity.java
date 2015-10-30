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
import java.net.URL;

import ru.ifmo.android_2015.citycam.download.DownloadFile;
import ru.ifmo.android_2015.citycam.download.ProgressCallback;
import ru.ifmo.android_2015.citycam.file.CreateFile;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.reader.Data;
import ru.ifmo.android_2015.citycam.reader.Reader;
import ru.ifmo.android_2015.citycam.save.Container;
import ru.ifmo.android_2015.citycam.save.SaveFragment;
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

    private SaveFragment saveFragment;
    private Container container;
    private ProgressBar progressBarView;
    private ImageView camImageView;
    private Button left, right;
    private TextView cam, user, cam_id;
    private DownloadFileTask downloadTask;
    private DownloadJsonTask downloadJsonTask;
    private City city;
    private int current_cam, per_page, page, all_cam;
    private Data[] data;
    private boolean change_orientation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_cam);

        progressBarView = (ProgressBar) findViewById(R.id.progress_bar);
        camImageView = (ImageView) findViewById(R.id.cam_image);
        cam = (TextView) findViewById(R.id.cam);
        user = (TextView) findViewById(R.id.user);
        cam_id = (TextView) findViewById(R.id.cam_id);
        left = (Button) findViewById(R.id.left_button);
        right = (Button) findViewById(R.id.right_button);

        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        getSupportActionBar().setTitle(city.name);
        left.setVisibility(View.INVISIBLE);
        right.setVisibility(View.INVISIBLE);
        cam.setVisibility(View.INVISIBLE);
        progressBarView.setMax(100);

        /**
         * Востанавливаем состояние после поворота экрана.
         */
        saveFragment = (SaveFragment) getFragmentManager().findFragmentByTag("SAVE_FRAGMENT");
        if (saveFragment != null) {
            container = saveFragment.getModel();
            downloadTask = container.downloadTask;
            downloadJsonTask = container.downloadJsonTask;
            data = container.data;
            current_cam = container.current_cam;
            per_page = container.per_page;
            page = container.page;
            all_cam = container.all_cam;
            change_orientation = true; // устанавливаем флаг, что экран был перевернут
            // и не требует повторной загрузки
        } else {
            saveFragment = new SaveFragment();
            getFragmentManager().beginTransaction().add(saveFragment, "SAVE_FRAGMENT")
                    .commit();
            current_cam = 1;
            per_page = 10;
            page = 1;
            data = null;
            change_orientation = false;
        }

        downloadJson();
    }

    public void downloadJson() {
        if (data == null) {
            if (downloadJsonTask == null) {
                // Создаем новый таск, только если не было ранее запущенного таска
                try {
                    URL url = Webcams.createNearbyUrl(city.latitude, city.longitude,
                            (current_cam / per_page) + 1);
                    // Создаем новый таск, только если не было ранее запущенного таска
                    downloadJsonTask = new DownloadJsonTask(this, url);
                    downloadJsonTask.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Передаем в ранее запущенный таск текущий объект Activity
                downloadJsonTask.attachActivity(this);
            }
        } else {
            // если данные уже загружены то загружаем картинку
            download();
        }
    }

    public void download() {
        progressBarView.setProgress(0);
        progressBarView.setVisibility(View.VISIBLE);
        if (downloadTask == null) {
            // Создаем новый таск, только если не было ранее запущенного таска
            downloadTask = new DownloadFileTask(this);
            downloadTask.execute();
        } else {
            // Передаем в ранее запущенный таск текущий объект Activity
            downloadTask.attachActivity(this);
        }
    }

    /**
     * Обрабатываем нажатие кнопок для перехода между камерами.
     */
    public void rightClick(View view) {
        if (current_cam < all_cam) {
            current_cam++;
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.INVISIBLE);
            cam.setVisibility(View.INVISIBLE);
            if (((current_cam - 1) / per_page) + 1 > page) {
                data = null;
                downloadJson();
            }
            download();
        }
    }

    public void leftClick(View view) {
        if (current_cam > 1) {
            current_cam--;
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.INVISIBLE);
            cam.setVisibility(View.INVISIBLE);
            download();
        }
    }

    /**
     * Сохраняем состояние экрана при разрушении активити.
     */
    @Override
    protected void onPause() {
        container = new Container(downloadJsonTask, downloadTask, data, current_cam,
                page, per_page, all_cam);
        saveFragment.setModel(container);
        super.onPause();
    }

    public static class DownloadJsonTask extends AsyncTask<Void, Void, Integer> {

        private URL url;
        private CityCamActivity activity;

        public DownloadJsonTask(CityCamActivity activity, URL url) {
            this.activity = activity;
            this.url = url;
        }

        public void attachActivity(CityCamActivity activity) {
            this.activity = activity;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                activity.data = Reader.downloadJson(url, activity.current_cam);
                return 1;
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            // Этот метод выполняется в UI потоке
            // Параметр resultCode -- это результат doInBackground
            activity.per_page = Reader.per_page;
            activity.all_cam = Reader.count;
            activity.page = Reader.page;
            activity.downloadJsonTask = null;
            activity.download(); // загружаем картинку после загрузки Json файла
        }
    }

    public static class DownloadFileTask extends AsyncTask<Void, Integer, Bitmap>
            implements ProgressCallback {

        private Context context;
        private CityCamActivity activity;
        private File destFile;

        private int progress;

        public DownloadFileTask(CityCamActivity activity) {
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
        protected Bitmap doInBackground(Void... params) {
            try {
                return downloadFile(context, this);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
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

        public void onPostExecute(Bitmap resultBit) {
            // Этот метод выполняется в UI потоке
            // Параметр resultCode -- это результат doInBackground
            activity.progressBarView.setVisibility(View.INVISIBLE);
            activity.left.setVisibility(View.VISIBLE);
            activity.right.setVisibility(View.VISIBLE);
            activity.cam.setVisibility(View.VISIBLE);
            if (activity.all_cam == 0)
                activity.cam.setText("Camera not found");
            else {
                activity.cam.setText(activity.current_cam + " of " + activity.all_cam);
                activity.user.setText("user : " + activity.data[activity.current_cam - 1].user);
                activity.cam_id.setText("id : " + activity.data[activity.current_cam - 1].id);
                if (resultBit != null)
                    activity.camImageView.setImageBitmap(resultBit);
            }
            activity.downloadTask = null;
        }

        private Bitmap downloadFile(Context context,
                                    ProgressCallback progressCallback) throws IOException {
            destFile = CreateFile.createTempExternalFile(context, ".jpg");

            if (!activity.change_orientation) {
                if (activity.data != null)
                    return DownloadFile.downloadFile(activity.data[activity.current_cam - 1].url,
                            destFile, progressCallback);
            } else {
                activity.change_orientation = false;
                return decodeFile(destFile.getPath()); // если экран был перевернут просто
                                                        // декодируем картинку
            }
            return null;
        }
    }

    private static final String TAG = "CityCam";
}
