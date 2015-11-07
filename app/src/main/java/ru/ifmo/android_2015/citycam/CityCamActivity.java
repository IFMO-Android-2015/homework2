package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

    private ImageView camImageView;
    private ProgressBar progressView;
    private ImageView onlineImageView;
    private RatingBar ratingBar;
    private TextView camnumTextView;
    private TextView infoTextView;

    private DownloadCamTask downloadCamTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        City city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        setContentView(R.layout.activity_city_cam);
        camImageView = (ImageView) findViewById(R.id.cam_image);
        progressView = (ProgressBar) findViewById(R.id.progress);
        onlineImageView = (ImageView) findViewById(R.id.online_image);
        ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        camnumTextView = (TextView) findViewById(R.id.camnum);
        infoTextView = (TextView) findViewById(R.id.info);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            downloadCamTask = (DownloadCamTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadCamTask == null) {
            downloadCamTask = new DownloadCamTask(this, city);
            downloadCamTask.execute(Integer.parseInt("" + camnumTextView.getText()));
        } else {
            downloadCamTask.attachActivity(this);
        }
    }

    public void previousCamera(View view) {
        switchCamera(Integer.parseInt("" + camnumTextView.getText()) - 2);
    }

    public void nextCamera(View view) {
        switchCamera(Integer.parseInt("" + camnumTextView.getText()));
    }

    public void reloadCamera(View view) {
        switchCamera(Integer.parseInt("" + camnumTextView.getText()) - 1);
    }

    private void switchCamera(int camnum) {
        City city = downloadCamTask.city;
        downloadCamTask.cancel(true);
        downloadCamTask = new DownloadCamTask(this, city);
        downloadCamTask.execute(camnum);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadCamTask;
    }

    enum DownloadState {
        DOWNLOADING, DONE, ERROR
    }

    static class DownloadCamTask extends AsyncTask<Integer, Void, DownloadState> {
        private CityCamActivity activity;
        private DownloadState state;
        private List<Cam> cams;
        private Bitmap bmp;
        private int camnum;
        private City city;

        DownloadCamTask(CityCamActivity activity, City city) {
            this.activity = activity;
            this.city = city;
            cams = new ArrayList<>();
        }

        public DownloadState getState() {
            return state;
        }

        void updateView() {
            if (isCancelled() || activity == null || state == null) {
                return;
            }
            switch (state) {
                case DONE:
                    activity.progressView.setVisibility(View.INVISIBLE);
                    activity.infoTextView.setText("Доступно камер: " + cams.size());

                    if (cams.isEmpty()) {
                        activity.camImageView.setImageBitmap(null);
                        activity.ratingBar.setRating(0);
                        activity.onlineImageView.setImageResource(R.drawable.red_circle);
                        activity.camnumTextView.setText("0");
                    } else {
                        Cam cam = cams.get(camnum);
                        activity.camImageView.setImageBitmap(bmp);
                        activity.ratingBar.setRating(cam.getRating());
                        activity.onlineImageView.setImageResource(R.drawable.green_circle);
                        activity.camnumTextView.setText("" + (camnum + 1));
                    }
                    break;
                case DOWNLOADING:
                    activity.progressView.setVisibility(View.VISIBLE);
                    activity.infoTextView.setText("Загрузка...");
                    activity.camImageView.setImageBitmap(null);
                    activity.ratingBar.setRating(0);
                    activity.onlineImageView.setImageResource(R.drawable.gray_circle);
                    break;
                case ERROR:
                    activity.progressView.setVisibility(View.INVISIBLE);
                    activity.infoTextView.setText("Ошибка");
                    activity.camImageView.setImageBitmap(null);
                    activity.ratingBar.setRating(0);
                    activity.onlineImageView.setImageResource(R.drawable.red_circle);
                    break;
            }
        }

        @Override
        protected void onPreExecute() {
            state = DownloadState.DOWNLOADING;
            updateView();
        }

        private Cam readCam(JsonReader reader) throws IOException {
            Boolean isActive = null;
            String url = null;
            Double rating = null;
            reader.beginObject();
            while (!isCancelled() && reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "active":
                        isActive = reader.nextInt() == 1;
                        break;
                    case "preview_url":
                        url = reader.nextString();
                        break;
                    case "rating_avg":
                        rating = reader.nextDouble();
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
            if (isCancelled() || isActive == null || url == null || rating == null) {
                return null;
            }
            return new Cam(isActive, url, rating);
        }

        private void readCams(JsonReader reader) throws IOException {
            reader.beginObject();
            while (!isCancelled() && reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("webcams")) {
                    reader.beginObject();

                    while (!isCancelled() && reader.hasNext()) {
                        name = reader.nextName();
                        if (name.equals("webcam")) {
                            reader.beginArray();
                            while (!isCancelled() && reader.hasNext()) {
                                Cam cam = readCam(reader);
                                if (cam != null) {
                                    cams.add(cam);
                                } else {
                                    Log.e(TAG, "Not all camera fields are exist");
                                }
                            }
                            reader.endArray();
                        } else {
                            reader.skipValue();
                        }
                    }

                    reader.endObject();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        }

        private void loadBitmap() throws IOException {
            URL url = new URL(cams.get(camnum).getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //            connection.setDoInput(true);
            //            connection.connect();
            //            InputStream input = connection.getInputStream();
            //            bmp = BitmapFactory.decodeStream(input);
            //            connection.disconnect();

            // Using byte reading instead of BitmapFactory.decodeStream() in order to be
            // able to stop this task immediately
            InputStream in = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try {
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new FileNotFoundException("Unexpected HTTP response: " +
                            responseCode + ", " + connection.getResponseMessage());
                }
                byte[] buffer = new byte[1024 * 128];
                int receivedBytes;
                in = connection.getInputStream();
                while (!isCancelled() && (receivedBytes = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, receivedBytes);
                }

                if (!isCancelled()) {
                    byte[] data = out.toByteArray();
                    bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close HTTP input stream: " + e, e);
                    }
                }
                out.close();
                connection.disconnect();
            }
        }

        @Override
        protected DownloadState doInBackground(Integer... parampam) {
            try {
                camnum = parampam[0];
                URL query = Webcams.createNearbyUrl(city.latitude, city.longitude);
                JsonReader reader = new JsonReader(new InputStreamReader(query.openStream
                        ()));

                Log.d(TAG, "start loading cams");
                readCams(reader);
                Log.d(TAG, "loaded " + cams.size() + " cams");
                if (isCancelled() || cams.isEmpty()) {
                    return state = DownloadState.DONE;
                }
                camnum = Math.min(cams.size() - 1, camnum);
                camnum = Math.max(0, camnum);
                loadBitmap();
                Log.d(TAG, "finish loading bitmap");

                state = DownloadState.DONE;

            } catch (Exception e) {
                Log.e(TAG, "Error downloading file: " + e, e);
                state = DownloadState.ERROR;
            }
            return state;
        }

        @Override
        protected void onPostExecute(DownloadState state) {
            // Проверяем код, который вернул doInBackground и показываем текст в зависимости
            // от результата
            this.state = state;
            updateView();
        }

        void attachActivity(CityCamActivity activity) {
            this.activity = activity;
            updateView();
        }

        @Override
        protected void onCancelled(DownloadState downloadState) {
            Log.d(TAG, "Async Task cancelled");
            super.onCancelled(downloadState);
        }
    }

    private static final String TAG = "CityCam";
}
