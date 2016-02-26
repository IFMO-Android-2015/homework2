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
    private DownloadImageTask downloadImageTask;
    private TextView webcamInfo;

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
        webcamInfo = (TextView) findViewById(R.id.textInfo);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.

        if (savedInstanceState != null) {
            downloadImageTask = (DownloadImageTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadImageTask == null) {
            downloadImageTask = new DownloadImageTask(this);
            downloadImageTask.execute(city);
        } else {
            downloadImageTask.attachActivity(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadImageTask;
    }

    private static class DownloadImageTask extends AsyncTask<City, Void, CameraInfo> {

        private CityCamActivity activity;
        private CameraInfo savedWebcam;

        public DownloadImageTask(CityCamActivity activity) {
            this.activity = activity;
            this.savedWebcam = null;
        }

        private void updateView() {
            if (savedWebcam == CameraInfo.BAD_INTERNET) {
                activity.progressView.setVisibility(ProgressBar.INVISIBLE);
                Toast.makeText(activity, "Bad internet connection", Toast.LENGTH_LONG).show();
            } else if (savedWebcam == CameraInfo.NO_WEBCAM) {
                activity.progressView.setVisibility(ProgressBar.INVISIBLE);
                Toast.makeText(activity, "There are no webcams :(", Toast.LENGTH_LONG).show();
            } else {
                activity.camImageView.setImageBitmap(savedWebcam.getImage());
                activity.progressView.setVisibility(ProgressBar.INVISIBLE);
                activity.webcamInfo.setText(savedWebcam.getTitle() + " : " + savedWebcam.getLatitude() + " " + savedWebcam.getLongitude());
            }
        }

        @Override
        protected CameraInfo doInBackground(City... params) {
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                CameraInfo webcam = city2webcam(params[0]);
                if (webcam != CameraInfo.BAD_INTERNET && webcam != CameraInfo.NO_WEBCAM) {
                    URL url = new URL(webcam.getUrl());
                    connection = (HttpURLConnection) url.openConnection();
                    inputStream = connection.getInputStream();
                    webcam.setImage(BitmapFactory.decodeStream(inputStream));
                }
                Log.e("WEB", webcam == null ? "null" : "not null");
                savedWebcam = webcam;
                return webcam;
            } catch (IOException e) {
                Log.e(TAG, "An error occurred while downloading image", e);
                return savedWebcam = CameraInfo.BAD_INTERNET;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ignored) {}
                }
            }
        }

        @Override
        protected void onPostExecute(CameraInfo ignored) {
            updateView();
        }

        private CameraInfo city2webcam(City city) throws IOException {
            URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);
            Log.e("JSON", url.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();
            try {
                JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));
                Log.e("JSON", jsonReader.toString());
                CameraInfo webcam = jsonReader2webcam(jsonReader);
                jsonReader.close();
                return webcam;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                connection.disconnect();
            }
        }

        private CameraInfo jsonReader2webcam(JsonReader in) throws IOException {
            in.beginObject();
            while (in.hasNext()) {
                String webcams = in.nextName();
                if (webcams.equals("webcams")) {
                    in.beginObject();
                    while (in.hasNext()) {
                        String webcamOrCount = in.nextName();
                        if (webcamOrCount.equals("count")) {
                            int count = in.nextInt();
                            if (count == 0) {
                                Log.e("COUNT", "No cameras available");
                                return CameraInfo.NO_WEBCAM;
                            }
                            in.skipValue();
                        }
                        if (webcamOrCount.equals("webcam")) {
                            in.beginArray();
                            CameraInfo ret = null;
                            while (in.hasNext()) {
                                in.beginObject();
                                if (ret == null) {
                                    ret = new CameraInfo();
                                }
                                while (in.hasNext()) {
                                    String name = in.nextName();
                                    switch (name) {
                                        case "title":
                                            ret.setTitle(in.nextString());
                                            break;
                                        case "latitude":
                                            ret.setLatitude(in.nextDouble());
                                            break;
                                        case "longitude":
                                            ret.setLongitude(in.nextDouble());
                                            break;
                                        case "preview_url":
                                            ret.setUrl(in.nextString());
                                            break;
                                        default:
                                            in.skipValue();
                                    }
                                }
                                in.endObject();
                            }
                            in.endArray();
                            in.endObject();
                            in.endObject();
                            return ret;
                        } else {
                            in.skipValue();
                        }
                    }
                    in.endObject();
                } else {
                    in.skipValue();
                }
            }
            in.endObject();
            return CameraInfo.BAD_INTERNET;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (savedWebcam != null) {
                updateView();
            }
        }

        public void attachActivity(CityCamActivity activity) {
            this.activity = activity;
            publishProgress();
        }

        private static final String TAG = "DownloadImageTask";
    }

    private static final String TAG = "CityCam";
}