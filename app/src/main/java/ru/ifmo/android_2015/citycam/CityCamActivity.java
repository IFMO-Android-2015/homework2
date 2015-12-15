package ru.ifmo.android_2015.citycam;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Экран, показывающий веб-камеру одного выбранного города.
 * Выбранный город передается в extra параметрах.
 */
public class CityCamActivity extends AppCompatActivity {


    private static class Downloader extends AsyncTask<Void, Void, Void> {
        private CityCamActivity activity;

        Downloader(CityCamActivity activity) {
            this.activity = activity;
        }

        public void attachActivity(CityCamActivity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            activity.updateUI();
            super.onPostExecute(aVoid);
        }


        HttpURLConnection conn;
        URL url;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.d("bg", "here");
                url = Webcams.createNearbyUrl(activity.city.latitude, activity.city.longitude);
                conn = (HttpURLConnection) url.openConnection();
                JsonReader reader = new JsonReader(new InputStreamReader(conn.getInputStream()));
                getInformation(reader);
                conn.disconnect();
                conn = (HttpURLConnection) url.openConnection();
                activity.image = BitmapFactory.decodeStream(conn.getInputStream());
                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        private void getInformation(JsonReader reader) throws Exception {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("webcams")) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        name = reader.nextName();
                        if (name.equals("webcam")) {
                            reader.beginArray();
                            while (reader.hasNext()) {
                                addInfo(reader);
                            }
                            reader.endArray();

                        } else {
                            reader.skipValue();
                        }
                    }
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        }

        private void addInfo(JsonReader reader) throws Exception {
            reader.beginObject();
            while (reader.hasNext()) {
                String info = reader.nextName();
                switch (info) {
                    case "preview_url": {
                        url = new URL(reader.nextString());
                        break;
                    }
                    case "view_count": {
                        activity.views = reader.nextInt();
                        break;
                    }
                    case "latitude": {
                        activity.latitude = reader.nextDouble();
                        break;
                    }
                    case "longitude": {
                        activity.longitude = reader.nextDouble();
                        break;
                    }
                    case "city": {
                        activity.cityName = reader.nextString();
                        break;
                    }
                    case "last_update": {
                        activity.date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
                                        .format(new Date(reader.nextInt()));
                        break;
                    }
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
        }
    }

    /**
     * Обязательный extra параметр - объект City, камеру которого надо показать.
     */
    public static final String EXTRA_CITY = "city";

    private City city;

    private ImageView camImageView;
    private ProgressBar progressView;
    private Bitmap image;
    private TextView errorText;
    private TextView infoText;

    private double latitude;
    private double longitude;
    private String cityName;
    private int views;
    private String date;
    static final private String LAT = "Latitude";
    static final private String LONG = "Longitude";
    static final private String CITYNAME = "Cityname";
    static final private String VIEWS = "Views";
    static final private String DATE = "Date";
    static final private String IMG = "Img";

    private Downloader downloader;


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
        infoText = (TextView) findViewById(R.id.info);
        errorText = (TextView) findViewById(R.id.err);

        infoText.setVisibility(View.INVISIBLE);
        errorText.setVisibility(View.INVISIBLE);
        progressView.setVisibility(View.VISIBLE);
        getSupportActionBar().setTitle(city.name);


        if (savedInstanceState != null) {
            this.downloader = (Downloader) getLastCustomNonConfigurationInstance();
        } else if (downloader == null) {
            downloader = new Downloader(this);
            downloader.execute();
        } else {
            downloader.attachActivity(this);
        }

    }


    void updateUI() {
        progressView.setVisibility(View.INVISIBLE);
        if (image != null) {
            camImageView.setImageBitmap(image);
            camImageView.setVisibility(View.VISIBLE);
            infoText.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.INVISIBLE);
            infoText.setText("Name: " + cityName + "\t\tDate: " + date +
                    "\nCoordinates: " + latitude + "\t\t" + longitude +
                    "\nViews: " + views);
        } else {
            errorText.setVisibility(View.VISIBLE);
            camImageView.setVisibility(View.INVISIBLE);
            infoText.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloader;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(VIEWS, views);
        outState.putString(DATE, date);
        outState.putString(CITYNAME, cityName);
        outState.putDouble(LAT, latitude);
        outState.putDouble(LONG, longitude);
        outState.putParcelable(IMG, image);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        this.views = savedInstanceState.getInt(VIEWS);
        this.date = savedInstanceState.getString(DATE);
        this.cityName = savedInstanceState.getString(CITYNAME);
        this.latitude = savedInstanceState.getDouble(LAT);
        this.longitude = savedInstanceState.getDouble(LONG);
        this.image = savedInstanceState.getParcelable(IMG);
        updateUI();
        super.onRestoreInstanceState(savedInstanceState);
    }


    private static final String TAG = "CityCam";
}
