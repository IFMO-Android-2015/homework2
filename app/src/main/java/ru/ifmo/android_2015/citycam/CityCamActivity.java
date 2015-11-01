package ru.ifmo.android_2015.citycam;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.WebcamData;
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

    private static City city;

    private static ImageView camImageView;
    private static ProgressBar progressView;

    private DownloadAndDecodeTask downloadAndDecodeTask;

    @Override
    @SuppressWarnings("deprecation")
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
        if (savedInstanceState != null) {
            // Пытаемся получить ранее запущенный таск
            downloadAndDecodeTask = (DownloadAndDecodeTask) getLastNonConfigurationInstance();
        }
        if (downloadAndDecodeTask == null) {
            // Создаем новый таск, только если не было ранее запущенного таска
            downloadAndDecodeTask = new DownloadAndDecodeTask(this);
            downloadAndDecodeTask.execute();
        } else {
            // Передаем в ранее запущенный таск текущий объект Activity
            downloadAndDecodeTask.attachActivity(this);
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
    private static final String TAG = "CityCam";

    static class DownloadAndDecodeTask extends AsyncTask<Void, Void, Void> {

        private Context appContext;
        private CityCamActivity activity;
        private static WebcamData data = new WebcamData();
        DownloadAndDecodeTask(CityCamActivity activity) {
            this.activity = activity;
            this.appContext = activity.getApplicationContext();
        }

        void attachActivity(CityCamActivity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressView.setVisibility(View.INVISIBLE);
            if (data.getBitmapURL().equals("")) {
                Toast.makeText(appContext, "К сожалению, в данном городе отсутствуют камеры", Toast.LENGTH_SHORT).show();
                activity.onBackPressed();
            }
            else {
                try {
                    camImageView.setImageDrawable(new BitmapDrawable(BitmapFactory.decodeStream((new URL(data.getBitmapURL())).openStream())));
                } catch (IOException e) {
                    Log.e(TAG, "Error getting bitmap from url " + e, e);
                }
            }

        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL cityURL = Webcams.createNearbyUrl(city.latitude, city.longitude);
                data = Parser.parse(cityURL);
            }
            catch (Exception e) {
                Log.e(TAG, "Error getting city url " + e, e);
                e.printStackTrace();
            }
            return null;
        }
    }

    static class Parser {
        static WebcamData parse(URL cityURL) {
            try {
                WebcamData data = new WebcamData();
                JsonReader reader = new JsonReader(new InputStreamReader(cityURL.openStream(), "UTF-8"));
                reader.beginObject();
                String name = reader.nextName();
                String picURLstr = "";
                while (reader.hasNext()) {
                    if (name.equals("webcams")) {
                        Log.i("PARSER", "found webcams");
                        reader.beginObject();
                        String curName = reader.nextName();
                        while (reader.hasNext()) {
                            if (curName.equals("webcam")) {
                                Log.i("PARSER", "found array webcam");
                                reader.beginArray();
                                reader.beginObject();
                                String insideArrayName = reader.nextName();
                                while (reader.hasNext()) {
                                    if (insideArrayName.equals("title")) {
                                        data.setTitle(reader.nextString());

                                        Log.i("PARSER", "found title = " + data.getTitle());
                                    }
                                    if (insideArrayName.equals("user")) {
                                        data.setUser(reader.nextString());

                                        Log.i("PARSER", "found user = " + data.getUser());
                                    }
                                    else if (insideArrayName.equals("preview_url")) {
                                        data.setBitmapURL(reader.nextString());
                                        Log.i("PARSER", "found preview_url = " + data.getBitmapURL());
                                        break;
                                    }
                                    else if (insideArrayName.equals("last_update")) {
                                        data.setTime(Long.parseLong(reader.nextString()));

                                        Log.i("PARSER", "found time = " + data.getTime());
                                    }
                                    else {
                                        reader.skipValue();
                                    }
                                    insideArrayName = reader.nextName();
                                }
                                break;
                            }
                            else {
                                reader.skipValue();
                                curName = reader.nextName();
                            }
                        }
                        break;
                    }
                    else {
                        reader.skipValue();
                        name = reader.nextName();
                    }
                }
                return data;
            } catch (Exception e) {
                Log.e(TAG, "Error parsing json " + e, e);
            }
            return null;
        }
    }
}

