package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    private TextView webcamInfo, webcamTitle;
    private Webcam webcam;
    DownloadWebcamInfoTask downloadTask;



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
        webcamInfo = (TextView) findViewById(R.id.info);
        webcamTitle = (TextView) findViewById(R.id.title);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.

        if (savedInstanceState != null) {
            downloadTask = (DownloadWebcamInfoTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask == null) {
            downloadTask = new DownloadWebcamInfoTask(this);
            downloadTask.execute(city);
        } else {
            downloadTask.attachActivity(this);
        }
    }

    @Override
    public Object getLastCustomNonConfigurationInstance() {
        return downloadTask;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("webcam", webcam);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        showWebcamInfo((Webcam) savedInstanceState.get("webcam"));
    }

    public void showWebcamInfo (Webcam webcam) {
        if (webcam != null) {
            progressView.setVisibility(View.INVISIBLE);
            camImageView.setImageBitmap(webcam.getImage());
            webcamInfo.setText("Latitude: " + webcam.getLatitude() + "\nLongitude: " + webcam.getLongitude() + "\nRating: " + webcam.getRating());
            webcamTitle.setText(webcam.getTitle());
        } else {
            webcamTitle.setText("Error");
        }
    }

    static class DownloadWebcamInfoTask extends AsyncTask<City, Void, Webcam> {
        CityCamActivity activity;

        public enum Status {ERROR, WORKING, FINISHED}
        private Status status;

        public Status getTaskStatus() {
            return status;
        }

        DownloadWebcamInfoTask(CityCamActivity activity) {
            this.activity = activity;
            status = Status.WORKING;
        }

        public void attachActivity(CityCamActivity activity) {
            this.activity = activity;
            if (status == Status.FINISHED)  {
                activity.showWebcamInfo(activity.webcam);
            }
        }



        @Override
        protected Webcam doInBackground(City... params) {
            status = Status.WORKING;
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
                Log.e(TAG, e.getMessage());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
            if (webcams.size() == 0) {
                return null;
            }
            Webcam webcam = webcams.get(0);
            try {
                url = new URL(webcam.getPreviewUrl());
                httpURLConnection = (HttpURLConnection) url.openConnection();

                inputStream = httpURLConnection.getInputStream();
                webcam.setImage(BitmapFactory.decodeStream(inputStream));
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                if(inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
            return webcam;
        }

        @Override
        protected void onPostExecute(Webcam webcam) {
            activity.webcam = webcam;
            status = Status.FINISHED;
            activity.showWebcamInfo(webcam);
        }

        protected List<Webcam> parseJSON(JsonReader jsonReader) throws IOException {
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
                                res.add(parseWebcamInfo(jsonReader));
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

        protected Webcam parseWebcamInfo(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();
            Webcam webcam = new Webcam();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                switch (name) {
                    case "latitude":
                        webcam.setLatitude(jsonReader.nextDouble());
                        break;
                    case "longitude":
                        webcam.setLongitude(jsonReader.nextDouble());
                        break;
                    case "rating_avg":
                        webcam.setRating(jsonReader.nextDouble());
                        break;
                    case "title":
                        webcam.setTitle(jsonReader.nextString());
                        break;
                    case "preview_url":
                        webcam.setPreviewUrl(jsonReader.nextString());
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return webcam;
        }
    }

    private static final String TAG = "CityCam";
}
