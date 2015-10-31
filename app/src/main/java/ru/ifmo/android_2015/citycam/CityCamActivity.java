package ru.ifmo.android_2015.citycam;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.json.*;
import org.w3c.dom.Text;


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
    private TextView camNameTextView;
    private TextView dateTextView;
    private TextView errorTextView;

    private static class GetCityCamTask extends AsyncTask<Void, Integer, Integer> {
        private CityCamActivity activity;
        private Bitmap imageBitmap;
        private String camName;
        private String date;
        private Integer result;

        @Override
        protected Integer doInBackground(Void... params) {
            Log.d("doInBackground", "started doing");
            int res;
            HttpURLConnection connect = null;
            try {
                URL url = Webcams.createNearbyUrl(activity.city.latitude, activity.city.longitude);
                connect = (HttpURLConnection) url.openConnection();
                String s = (new java.util.Scanner(connect.getInputStream()).useDelimiter("\\A")).next();
                connect.disconnect();

                Log.d("GetCityCam: Response", s);
                JSONObject root = new JSONObject(s);
                JSONObject firstCam = root.getJSONObject("webcams").getJSONArray("webcam").getJSONObject(0);
                camName = firstCam.getString("title");
                SimpleDateFormat dateFromat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                date = dateFromat.format(firstCam.getLong("last_update") * 1000);
                url = new URL(firstCam.getString("preview_url"));
                connect = (HttpURLConnection) url.openConnection();
                imageBitmap = BitmapFactory.decodeStream(connect.getInputStream());
                connect.disconnect();
                res = 0;
            } catch(Exception ex) {
                Log.d("GetCityCam: Exception", ex.getMessage());
                if(ex instanceof JSONException && ex.getMessage().contains("out of range")) {
                    res = 2;
                } else {
                    res = 1;
                }
            } finally {
                if(connect != null) {
                    connect.disconnect();
                }
            }
            return res;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            this.result = result;
            setBitmap();
        }

        public void setBitmap() {
            if(result == 0) {
                if (imageBitmap != null) {
                    activity.camImageView.setImageBitmap(imageBitmap);
                    activity.progressView.setVisibility(View.INVISIBLE);
                    activity.camNameTextView.setText("Camera name:\n" + camName);
                    activity.camNameTextView.setVisibility(View.VISIBLE);
                    activity.dateTextView.setText("Date:\n" + date);
                    activity.dateTextView.setVisibility(View.VISIBLE);
                }
            } else if(result == 1) {
                activity.progressView.setVisibility(View.INVISIBLE);
                activity.errorTextView.setVisibility(View.VISIBLE);
            } else if(result == 2) {
                activity.progressView.setVisibility(View.INVISIBLE);
                activity.errorTextView.setText("No cameras in this city");
                activity.errorTextView.setVisibility(View.VISIBLE);
            }
        }

        GetCityCamTask(CityCamActivity activity) {
            attachActivity(activity);
        }

        public void attachActivity(CityCamActivity activity) {
            this.activity = activity;
        }
    }

    private GetCityCamTask fetchTask;

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
        camNameTextView = (TextView) findViewById(R.id.camNameTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        errorTextView = (TextView) findViewById(R.id.errorTextView);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);


        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.

        if(savedInstanceState != null) {
            fetchTask = (GetCityCamTask) getLastCustomNonConfigurationInstance();
        }
        if(fetchTask == null) {
            fetchTask = new GetCityCamTask(this);
            fetchTask.execute();
        } else {
            fetchTask.attachActivity(this);
            fetchTask.setBitmap();
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return fetchTask;
    }

    private static final String TAG = "CityCam";
}
