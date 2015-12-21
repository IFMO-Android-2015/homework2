package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

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

class DownloadAsyncTask extends AsyncTask<City, Void, Integer> {

    CityCamActivity cityCamActivity;
    Bitmap bitmap;
    String url;

    enum State {
        DOWNLOAD,
        READY
    }

    State state;

    DownloadAsyncTask(CityCamActivity cityCamActivity) {
        this.cityCamActivity = cityCamActivity;
    }

    @Override
    protected Integer doInBackground(City... city) {
        state = State.DOWNLOAD;
        try {
            getUrl(city[0]);
            loadBitmap();
        } catch (Exception e) {
            Log.e("failed to load image: ", e.getMessage());
            return 1;
        }
        return 0;
    }

    private void loadBitmap() throws Exception {
        HttpURLConnection imageConnection = null;
        InputStream inputStream = null;
        try {
            imageConnection = (HttpURLConnection) (new URL(url)).openConnection();

            inputStream = imageConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } finally {
            if(imageConnection != null) {
                imageConnection.disconnect();
            }
        }
    }

    private void getUrl(City city) throws Exception {

        HttpURLConnection httpConnection = null;
        InputStreamReader inputStreamReader = null;
        JsonReader jsonReader = null;

        try {
            URL imageInfoURL = Webcams.createNearbyUrl(city.latitude, city.longitude);

            httpConnection = (HttpURLConnection) imageInfoURL.openConnection();



            if(httpConnection.getResponseCode() != 200) {
                throw new Exception("wrong response code");
            }

            inputStreamReader = new InputStreamReader(httpConnection.getInputStream(), "UTF-8");

            jsonReader = new JsonReader(inputStreamReader);

            while(jsonReader.hasNext()) {
                if(jsonReader.peek() == JsonToken.BEGIN_OBJECT) {
                    jsonReader.beginObject();
                }

                String switchArg = jsonReader.nextName();
                Log.e("switchArg: ", switchArg);
                switch(switchArg) {
                    case "status":
                        String status = jsonReader.nextString();
                        if(!status.equals("ok")) {
                            throw new Exception("status is not ok: " + status);
                        }
                        break;
                    case "webcams":
                        jsonReader.beginObject();
                        break;
                    case "webcam":
                        jsonReader.beginArray();
                        break;
                    case "count":
                        if(jsonReader.nextInt() == 0) {
                            throw new Exception("no cams");
                        }
                        break;
                    case "preview_url":
                        url = jsonReader.nextString();
                        break;
                    default:
                        jsonReader.skipValue();
                        break;
                }
            }

        } catch(Exception e) {
            throw e;
        } finally {
            if(httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    protected void onPostExecute(Integer result) {

        if(result != 0) {
            cityCamActivity.onBitmapFailed();
        } else {
            state = State.READY;
            cityCamActivity.onBitmapReady(bitmap);
        }
    }
}


public class CityCamActivity extends AppCompatActivity {

    /**
     * Обязательный extra параметр - объект City, камеру которого надо показать.
     */
    public static final String EXTRA_CITY = "city";

    private City city;

    private ImageView camImageView;
    private ProgressBar progressView;
    private DownloadAsyncTask downloadAsyncTask;

    void onBitmapReady(Bitmap bitmap) {
        Log.e("result: ", "bitmap ready");
        camImageView.setImageBitmap(bitmap);
        progressView.setVisibility(View.INVISIBLE);
    }
     void onBitmapFailed() {
         Log.e("result: ", "bitmap is failed");
         camImageView.setImageDrawable(getResources().getDrawable((R.drawable.no)));
         progressView.setVisibility(View.INVISIBLE);
    }

    public Object onRetainCustomNonConfigurationInstance() {
        return downloadAsyncTask;
    }

    private void createNewTask() {
        downloadAsyncTask = new DownloadAsyncTask(this);
        downloadAsyncTask.execute(city);
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

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.

        if(savedInstanceState != null) {
            downloadAsyncTask = (DownloadAsyncTask) getLastCustomNonConfigurationInstance();
            if(downloadAsyncTask == null) {
                createNewTask();
            } else {
                if(downloadAsyncTask.state == DownloadAsyncTask.State.READY) {
                    camImageView.setImageBitmap(downloadAsyncTask.bitmap);
                    progressView.setVisibility(View.INVISIBLE);
                } else {
                    createNewTask();
                }
            }
        } else {
            createNewTask();
        }

    }

    private static final String TAG = "CityCam";
}
