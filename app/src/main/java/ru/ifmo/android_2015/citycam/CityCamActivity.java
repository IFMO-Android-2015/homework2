package ru.ifmo.android_2015.citycam;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Camera;
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
    private TextView addInfo;
    private Camera camera;
    DownloadTask downloadTask;

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
        addInfo = (TextView) findViewById(R.id.addInfo);
        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
        if (savedInstanceState != null) {
            downloadTask = (DownloadTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask == null) {
            downloadTask = new DownloadTask(this);
            downloadTask.execute(city);
        } else {
            downloadTask.attackActivity(this);
        }
    }

    @Override
    public Object getLastCustomNonConfigurationInstance() {
        return downloadTask;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("camera", camera);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        showCamera((Camera)savedInstanceState.get("camera"));
    }

    public void showCamera (Camera camera) {
        if (camera != null) {
            progressView.setVisibility(View.INVISIBLE);
            camImageView.setImageBitmap(camera.getPreview());
            addInfo.setText("Широта: " + camera.getLatitude() + "\nДолгота: " + camera.getLongitude() + "\nРейтинг: " + camera.getRating());
        } else {
            Toast.makeText(this, "Произошла ошибка.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    static class DownloadTask extends AsyncTask<City, Integer, Camera> {
        CityCamActivity activity;
        private Status status = Status.PENDING;

        DownloadTask(CityCamActivity activity) {
            this.activity = activity;
        }

        @Override
        protected Camera doInBackground(City... params) {
            status = Status.RUNNING;
            HttpURLConnection httpURLConnection = null;
            InputStream inputStream = null;
            URL url;
            List<Camera> cameras = new ArrayList<>();
            try {
                url = Webcams.createNearbyUrl(params[0].latitude, params[0].longitude);
                httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.connect();

                inputStream = httpURLConnection.getInputStream();
                JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
                cameras = parseJSON(reader);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (cameras.size() == 0) {
                return null;
            }
            Camera camera = cameras.get(new Random(System.currentTimeMillis()).nextInt(cameras.size()));
            try {
                url = new URL(camera.getPreview_url());
                httpURLConnection = (HttpURLConnection) url.openConnection();

                inputStream = httpURLConnection.getInputStream();
                camera.setPreview(BitmapFactory.decodeStream(inputStream));
            }
            catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return camera;
        }

        @Override
        protected void onPostExecute(Camera camer) {
            activity.camera = camer;
            status = Status.FINISHED;
            activity.showCamera(camer);
        }

        protected List<Camera> parseJSON(JsonReader jsonReader) throws IOException{
            List<Camera> res = new ArrayList<>();
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if (name.equals("webcams")) {
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        name = jsonReader.nextName();
                        if (name.equals("webcam")) {
                            jsonReader.beginArray();
                            while (jsonReader.hasNext()) {
                                res.add(parseCameraInfo(jsonReader));
                            }
                            jsonReader.endArray();
                        } else {
                            jsonReader.skipValue();
                        }
                    }
                    jsonReader.endObject();
                } else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return res;
        }

        protected Camera parseCameraInfo(JsonReader jsonReader) throws IOException{
            jsonReader.beginObject();
            Camera camera = new Camera();
            while (jsonReader.hasNext()) {
                String tag = jsonReader.nextName();
                switch (tag) {
                    case "title":
                        camera.setTitle(jsonReader.nextString());
                        break;
                    case "preview_url":
                        camera.setPreview_url(jsonReader.nextString());
                        break;
                    case "longitude":
                        camera.setLongitude(jsonReader.nextDouble());
                        break;
                    case "latitude":
                        camera.setLatitude(jsonReader.nextDouble());
                        break;
                    case "rating_avg":
                        camera.setRating(jsonReader.nextDouble());
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return camera;
        }

        public void attackActivity(CityCamActivity cityCamActivity) {
            activity = cityCamActivity;
            if (status == status.FINISHED) {
                activity.showCamera(activity.camera);
            }
        }
    }

    private static final String TAG = "CityCam";
}
