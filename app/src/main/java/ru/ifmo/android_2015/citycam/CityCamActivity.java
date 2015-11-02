package ru.ifmo.android_2015.citycam;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    private ImageView camImageView;
    private ProgressBar progressView;

    private TextView titleText;
    private TextView idText;
    private TextView latitudeText;
    private TextView longitudeText;

    private ImageDownloadTask downloadTask;


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
        titleText = (TextView) findViewById(R.id.title_info);
        idText = (TextView) findViewById(R.id.cam_id_info);
        latitudeText = (TextView) findViewById(R.id.latitude_info);
        longitudeText = (TextView) findViewById(R.id.longitude_info);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            downloadTask = (ImageDownloadTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask == null) {
            downloadTask = new ImageDownloadTask();
            downloadTask.attachActivity(this);
            downloadTask.execute(city);
        } else {
            downloadTask.attachActivity(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadTask;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("TITLE_TEXT", titleText.getText().toString());
        savedInstanceState.putString("ID_TEXT", idText.getText().toString());
        savedInstanceState.putString("LATITUDE_TEXT", latitudeText.getText().toString());
        savedInstanceState.putString("LONGITUDE_TEXT", longitudeText.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            titleText.setText(savedInstanceState.getString("TITLE_TEXT"));
            idText.setText(savedInstanceState.getString("ID_TEXT"));
            latitudeText.setText(savedInstanceState.getString("LATITUDE_TEXT"));
            longitudeText.setText(savedInstanceState.getString("LONGITUDE_TEXT"));
        }
    }

    static public class ImageDownloadTask extends AsyncTask<City, Void, WebCam> {
        private WebCam webcam;

        private CityCamActivity activity;

        public enum Status {CREATED, DONE, ERROR, IN_PROGRESS}

        private Status status = Status.CREATED;

        public void attachActivity(CityCamActivity activity) {
            this.activity = activity;
            updateImage();
        }

        private void updateImage() {
            switch (status){
                case DONE:
                    activity.progressView.setVisibility(View.GONE);
                    activity.idText.setVisibility(View.VISIBLE);
                    activity.titleText.setVisibility(View.VISIBLE);
                    activity.longitudeText.setVisibility(View.VISIBLE);
                    activity.latitudeText.setVisibility(View.VISIBLE);
                    activity.camImageView.setImageBitmap(webcam.image);
                    break;
                case ERROR:
                    activity.progressView.setVisibility(View.GONE);
                    Bitmap mBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.badnews);
                    activity.camImageView.setImageBitmap(mBitmap);
                    break;
            }
        }

        @Override
        protected WebCam doInBackground(City... params) {
            try {
                status = Status.IN_PROGRESS;
                webcam = getWebCam(params[0]);

                if (webcam != null) {
                    webcam.loadImage();
                    if (webcam.image == null) {
                        status = Status.ERROR;
                    } else {
                        status = Status.DONE;
                    }
                } else {
                    status = Status.ERROR;
                }

                return webcam;
            } catch (Exception e) {
                status = Status.ERROR;
                return null;
            }
        }

        @Override
        protected void onPostExecute(WebCam webCam) {
            updateImage();

            if (status.equals(Status.ERROR)) {

            } else {
                activity.idText.setText("ID: " + webCam.id);
                activity.titleText.setText("Заголовок: " + webCam.title);
                activity.longitudeText.setText("Долгота: " + webCam.longitude);
                activity.latitudeText.setText("Широта: " + webCam.latitude);
            }
        }

        private WebCam getWebCam(City city) throws IOException {
            URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();

            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            List<WebCam> result = getListOfWebCams(reader);
            reader.close();

            if (inputStream != null) {
                inputStream.close();
            }

            connection.disconnect();

            if (result.isEmpty()) {
                return null;
            } else {
                return result.get(0);
            }
        }

        private List<WebCam> getListOfWebCams(JsonReader jsonReader) throws IOException {
            List<WebCam> result = null;
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String term = jsonReader.nextName();
                if (term.equals("webcams")) {
                    result = readWebCamsList(jsonReader);
                } else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();

            return result;
        }

        private List<WebCam> readWebCamsList(JsonReader jsonReader) throws IOException {
            List<WebCam> result = new ArrayList<>();

            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String term = jsonReader.nextName();
                if (term.equals("webcam")) {
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        result.add(new WebCam(jsonReader));
                    }
                    jsonReader.endArray();
                } else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();

            return result;
        }

    }

    private static final String TAG = "CityCam";
}
