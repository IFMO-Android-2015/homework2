package ru.ifmo.android_2015.citycam;

import android.app.Activity;
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
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    private Webcam SelectedCam;
    private DownloadFilesTask Task;
    private Bitmap bitmapImage;

    private ImageView camImageView;
    private ProgressBar progressView;
    private TextView titleView, cityView, countryView;



    private class Webcam {
        String title, country, city, viewURL;

    }

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
        cityView =  (TextView) findViewById(R.id.city);
        titleView =  (TextView) findViewById(R.id.title);
        countryView = (TextView) findViewById(R.id.country);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            Task = (DownloadFilesTask) getLastCustomNonConfigurationInstance();
        }
        if (Task == null) {
            Task = new DownloadFilesTask(this);
            Task.execute(city);
        } else {
            Task.changeActivity(this);
        }

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
    }

    private void setView(Bitmap bitmapImage) {
        progressView.setVisibility(View.INVISIBLE);
        camImageView.setImageBitmap(bitmapImage);
        countryView.setText("country:" + SelectedCam.country);
        titleView.setText("title:" + SelectedCam.title);
        cityView.setText("city:" + SelectedCam.city);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return Task;
    }


    private class DownloadFilesTask extends AsyncTask<City, Void, Void> {
        CityCamActivity activity;

        DownloadFilesTask(CityCamActivity activity) {
            this.activity = activity;
        }

        private InputStream getInputStreamFromConnection(HttpURLConnection conn) throws IOException {
            InputStream is = null;
            try {
                conn.setReadTimeout(10000/* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "The response is: " + response);
                is = conn.getInputStream();
            } catch (IOException e) {
                Log.d(TAG, "Connection error");
            }
            return is;
        }


        private Bitmap getImageFromUrl(String myURL) throws IOException {
            Bitmap webcamImage;
            InputStream imageStream = null;
            URL url = new URL(myURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                imageStream = getInputStreamFromConnection(conn);
                Log.d(TAG, "Download picture");
                webcamImage = BitmapFactory.decodeStream(imageStream);
            } finally {
                conn.disconnect();
                if (imageStream != null) {
                    imageStream.close();
                }
            }
            return webcamImage;
        }


        public void readJsonStream(InputStream in) throws IOException {
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

            try {
                Log.d(TAG, "Go to Webcams");
                reader.beginObject();
                if (reader.nextName().equals("status")) {
                    String status = reader.nextString();
                    if (status.equals("ok")) {
                       reader.nextName();
                        reader.beginObject();
                        SelectedCam = readWebcams(reader);
                        reader.endObject();
                    }
                }
            } finally {
                reader.endObject();
                reader.close();
            }
        }

        public Webcam readWebcams(JsonReader reader) throws IOException {
            Log.d(TAG, "Go to Array");
            Log.d(TAG, Boolean.toString(reader == null));
            try {
                int count = 1;
                while (reader.hasNext()) {

                    String name = reader.nextName();
                    if (name.equals("count")) {
                        count = reader.nextInt();
                    } else {
                        if (name.equals("webcam")) {
                            return readWebcam(reader, count);
                        } else {
                            reader.nextString();
                        }
                    }


                }
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            } finally {
                // reader.endObject();

            }
            return new Webcam();

        }

        private Webcam readWebcam(JsonReader reader, int count) throws IOException {
            Log.d(TAG, "yes");
            reader.beginArray();
            if (count != 0) {

                reader.beginObject();
                Webcam SelectedCam = new Webcam();
                while (reader.hasNext()) {
                    String currentName = reader.nextName();
                    Log.d(TAG, currentName);
                    switch (currentName) {
                        case "title":
                            SelectedCam.title = reader.nextString();
                            Log.d(TAG, SelectedCam.title);
                            break;
                        case "preview_url":
                            SelectedCam.viewURL = reader.nextString();
                            Log.d(TAG, SelectedCam.viewURL);
                            break;
                        case "city":
                            SelectedCam.city = reader.nextString();
                            break;
                        case "country":
                            SelectedCam.country = reader.nextString();
                            break;
                        default:
                            reader.skipValue();
                            break;
                    }
                }
                reader.endObject();
                Log.d(TAG, "what?");
                while (reader.hasNext()) {
                    reader.skipValue();
                }

                reader.endArray();
                return SelectedCam;
            } else {
                Log.d(TAG, "ending");
                reader.endArray();
                return null;
            }
        }


        @Override
        protected Void doInBackground(City... params) {
            try {

                InputStream jsonStream = null;
                URL cityUrl = Webcams.createNearbyUrl(params[0].latitude, params[0].longitude);
                System.err.println(cityUrl);
                HttpURLConnection conn = (HttpURLConnection) cityUrl.openConnection();
                Log.d(TAG, "start to Download json");
                jsonStream = getInputStreamFromConnection(conn);
                Log.d(TAG, "Download json");
                readJsonStream(jsonStream);
                conn.disconnect();
                if (jsonStream != null) {
                    jsonStream.close();
                }
                if (SelectedCam != null) {
                    bitmapImage = getImageFromUrl(SelectedCam.viewURL);

                }

            } catch (IOException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (SelectedCam != null) {
                activity.setView(bitmapImage);
            }
        }

        void changeActivity(CityCamActivity activity) {
            this.activity = activity;
        }


    }


    private static final String TAG = "CityCam";
}
