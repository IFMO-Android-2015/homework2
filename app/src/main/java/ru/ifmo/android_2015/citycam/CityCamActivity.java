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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

import android.util.JsonReader;


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
            JsonReader json = null;
            InputStream inp = null;
            try {
                URL url = Webcams.createNearbyUrl(activity.city.latitude, activity.city.longitude);
                connect = (HttpURLConnection) url.openConnection();

                inp = connect.getInputStream();
                json = new JsonReader(new InputStreamReader(inp));
                SimpleDateFormat dateFromat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                json.beginObject();
                boolean hasCams = false;
                while(json.hasNext()) {
                    if(json.nextName().equals("webcams")) {
                        json.beginObject();
                        while(json.hasNext()) {
                            if(json.nextName().equals("webcam")) {
                                json.beginArray();
                                while(json.hasNext()) {
                                    hasCams = true;
                                    json.beginObject();
                                    while (json.hasNext()) {
                                        String name = json.nextName();
                                        switch (name) {
                                            case "title":
                                                activity.camName = json.nextString();
                                                break;
                                            case "last_update":
                                                activity.date = dateFromat.format(json.nextLong() * 1000);
                                                break;
                                            case "preview_url":
                                                url = new URL(json.nextString());
                                                break;
                                            default:
                                                json.skipValue();
                                                break;
                                        }
                                    }
                                    json.endObject();
                                }
                                json.endArray();
                            } else {
                                json.skipValue();
                            }
                        }
                        json.endObject();
                    } else {
                        json.skipValue();
                    }
                }
                json.endObject();
                json.close();
                inp.close();
                connect.disconnect();
                if(hasCams) {
                    connect = (HttpURLConnection) url.openConnection();
                    inp = connect.getInputStream();
                    Bitmap tmp = BitmapFactory.decodeStream(inp);
                    inp.close();
                    activity.imageBitmap = tmp;
                    connect.disconnect();
                    activity.downloadResult = 0;
                } else {
                    activity.downloadResult = 2;
                }
            } catch(Exception ex) {
                Log.d("GetCityCam: Exception", ex.getMessage());
                activity.downloadResult = 1;
            } finally {
                if(connect != null) {
                    connect.disconnect();
                }
                if(json != null) {
                    try {
                        json.close();
                    } catch (IOException e) {}
                }
                if(inp != null) {
                    try {
                        inp.close();
                    } catch (IOException e) {}
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
