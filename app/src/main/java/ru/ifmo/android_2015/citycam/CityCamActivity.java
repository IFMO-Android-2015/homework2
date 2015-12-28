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
    private Camera camera;
    private DownloadTask downloadTask;

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

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
        if (savedInstanceState != null) {
            downloadTask = (DownloadTask)getLastCustomNonConfigurationInstance();
        }
        if (downloadTask != null) {
            downloadTask.attachActivity(this);
        } else {
            downloadTask = new DownloadTask(this);
            downloadTask.execute(city);
        }
    }

    public void showCamera(Camera camera) {
        if (camera == null) {
            Toast.makeText(this, "Ошибка при загрузке", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        progressView.setVisibility(View.INVISIBLE);
        camImageView.setImageBitmap(camera.preview);
        ((TextView)findViewById(R.id.textView)).setText("Широта: " + camera.latitude + "\nДолгота: " + camera.longitude);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
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
        showCamera((Camera) savedInstanceState.get("camera"));
    }

    static class DownloadTask extends AsyncTask<City, Integer, Camera> {
        CityCamActivity cityCamActivity;
        Camera camera;
        private Status status = Status.PENDING;

        DownloadTask (CityCamActivity activity) {
            this.cityCamActivity = activity;
        }

        public void attachActivity(CityCamActivity activity) {
            this.cityCamActivity = activity;
            this.cityCamActivity.camera = camera;
            if (status == Status.FINISHED) {
                cityCamActivity.showCamera(this.camera);
            }
        }

        @Override
        protected Camera doInBackground(City... params) {
            status = Status.RUNNING;
            HttpURLConnection httpURLConnection = null;
            InputStream inputStream = null;
            URL url;
            Camera camera = null;
            try {
                url = Webcams.createNearbyUrl(params[0].latitude, params[0].longitude);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.connect();

                inputStream = httpURLConnection.getInputStream();
                JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));
                camera = parseJson(jsonReader);
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
            if (camera == null) return null;
            try {
                url = new URL(camera.preview_url);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                inputStream = httpURLConnection.getInputStream();
                camera.preview = BitmapFactory.decodeStream(inputStream);
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
            return camera;
        }

        Camera parseJson(JsonReader jsonReader) throws IOException{
            Camera res = null;
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if (!name.equals("webcams")) {
                    jsonReader.skipValue();
                    continue;
                }
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    name = jsonReader.nextName();
                    if (!name.equals("webcam")) {
                        jsonReader.skipValue();
                        continue;
                    }
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        res = parseCamera(jsonReader);
                    }
                    jsonReader.endArray();
                }
                jsonReader.endObject();
            }
            jsonReader.endObject();
            return res;
        }

        Camera parseCamera(JsonReader jsonReader) throws IOException{
            Camera camera = new Camera();
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                switch (name) {
                    case "longitude":
                        camera.longitude = jsonReader.nextDouble();
                        break;
                    case "latitude":
                        camera.latitude = jsonReader.nextDouble();
                        break;
                    case "title":
                        camera.title = jsonReader.nextString();
                        break;
                    case "preview_url":
                        camera.preview_url = jsonReader.nextString();
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return camera;
        }

        @Override
        protected void onPostExecute(Camera camera) {
            status = Status.FINISHED;
            cityCamActivity.camera = camera;
            this.camera = camera;
            cityCamActivity.showCamera(cityCamActivity.camera);
        }
    }

    private static final String TAG = "CityCam";
}
