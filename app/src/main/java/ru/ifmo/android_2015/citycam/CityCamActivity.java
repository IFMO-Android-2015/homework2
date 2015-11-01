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

import org.json.*;


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

    private Bitmap imageBitmap;
    private String camName;
    private String date;
    private Integer downloadResult = -1;

    private static class GetCityCamTask extends AsyncTask<Void, Integer, Void> {
        private CityCamActivity activity;

        @Override
        protected Void doInBackground(Void... params) {
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
                activity.camName = firstCam.getString("title");
                SimpleDateFormat dateFromat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                activity.date = dateFromat.format(firstCam.getLong("last_update") * 1000);
                url = new URL(firstCam.getString("preview_url"));
                connect = (HttpURLConnection) url.openConnection();
                Bitmap tmp = BitmapFactory.decodeStream(connect.getInputStream());
                activity.imageBitmap = tmp;
                connect.disconnect();
                activity.downloadResult = 0;
            } catch(Exception ex) {
                Log.d("GetCityCam: Exception", ex.getMessage());
                if(ex instanceof JSONException && ex.getMessage().contains("out of range")) {
                    activity.downloadResult = 2;
                } else {
                    activity.downloadResult = 1;
                }
            } finally {
                if(connect != null) {
                    connect.disconnect();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            activity.setBitmap();
            super.onPostExecute(result);
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

        if(savedInstanceState != null) {
            fetchTask = (GetCityCamTask) getLastCustomNonConfigurationInstance();
        }
        if(fetchTask == null) {
            fetchTask = new GetCityCamTask(this);
            fetchTask.execute();
        } else {
            fetchTask.attachActivity(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return fetchTask;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putString("camName", camName);
        state.putString("date", date);
        state.putInt("downloadResult", downloadResult);
        state.putParcelable("imageBitmap", imageBitmap);
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        camName = state.getString("camName");
        date = state.getString("date");
        downloadResult = state.getInt("downloadResult");
        imageBitmap = state.getParcelable("imageBitmap");
        setBitmap();
        super.onRestoreInstanceState(state);
    }

    private void setBitmap() {
        if(downloadResult == 0) {
            if (imageBitmap != null) {
                camImageView.setImageBitmap(imageBitmap);
                progressView.setVisibility(View.INVISIBLE);
                camNameTextView.setText("Camera name:\n" + camName);
                camNameTextView.setVisibility(View.VISIBLE);
                dateTextView.setText("Date:\n" + date);
                dateTextView.setVisibility(View.VISIBLE);
            }
        } else if(downloadResult == 1) {
            progressView.setVisibility(View.INVISIBLE);
            errorTextView.setVisibility(View.VISIBLE);
        } else if(downloadResult == 2) {
            progressView.setVisibility(View.INVISIBLE);
            errorTextView.setText("No cameras in this city");
            errorTextView.setVisibility(View.VISIBLE);
        }
    }

    private static final String TAG = "CityCam";
}
