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

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.*;

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
    private Webcam webcam;
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
            downloadTask.attachActivity(this);
        }
    }

    public void showCamera (Webcam webcam) {
        if (webcam != null) {
            progressView.setVisibility(View.INVISIBLE);
            camImageView.setImageBitmap(webcam.getPreview());
            addInfo.setText("Широта: " + webcam.getLatitude() + "\n" +
                            "Долгота: " + webcam.getLongitude() + "\n" +
                            "Рейтинг: " + webcam.getRating()
            );
        } else {
            Toast toast = Toast.makeText(this, "Очень жаль...", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    static class DownloadTask extends AsyncTask<City, Integer, Webcam> {
        private CityCamActivity activity;
        private Status status = Status.PENDING;

        DownloadTask(CityCamActivity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPostExecute(Webcam webcam) {
            activity.webcam = webcam;
            status = Status.FINISHED;
            activity.showCamera(webcam);
        }

        public void attachActivity(CityCamActivity cityCamActivity) {
            activity = cityCamActivity;
            if (status == status.FINISHED) {
                activity.showCamera(activity.webcam);
            }
        }

        @Override
        protected Webcam doInBackground(City... params) {
            status = Status.RUNNING;
            HttpURLConnection httpURLConnection = null;
            InputStream inputStream = null;
            URL url;
            List<Webcam> webcams = new ArrayList<>();
            try {
                url = Webcams.createNearbyUrl(params[0].latitude, params[0].longitude);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.connect();
                inputStream = httpURLConnection.getInputStream();
                JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
                webcams = parseJSON(reader);
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

            if (webcams.size() == 0) {
                return null;
            }

            Webcam webcam = webcams.get(0);
            try {
                url = new URL(webcam.getPreview_url());
                httpURLConnection = (HttpURLConnection) url.openConnection();
                inputStream = httpURLConnection.getInputStream();
                webcam.setPreview(BitmapFactory.decodeStream(inputStream));
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
            return webcam;
        }

        protected List<Webcam> parseJSON(JsonReader jsonReader) throws IOException{
            List<Webcam> res = new ArrayList<>();
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

        protected Webcam parseCameraInfo(JsonReader jsonReader) throws IOException{
            jsonReader.beginObject();
            Webcam camera = new Webcam();
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
    }

    private static final String TAG = "CityCam";
}
