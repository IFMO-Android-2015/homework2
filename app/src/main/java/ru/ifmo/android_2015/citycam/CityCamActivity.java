package ru.ifmo.android_2015.citycam;

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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

import static android.graphics.BitmapFactory.*;

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
    Cams cams;
    Bitmap bitmap;

    private static class Cams extends AsyncTask<City, Void, Bitmap> {


        private CityCamActivity activity;

        Cams(CityCamActivity activity) {
            this.activity = activity;
        }



        protected Bitmap doInBackground(City... params) {
            JsonReader js = null;
            InputStream str = null;
            HttpURLConnection con = null;
            try {
                URL url = Webcams.createNearbyUrl(activity.city.latitude, activity.city.longitude);
                con = (HttpURLConnection) url.openConnection();
                str = con.getInputStream();
                js = new JsonReader(new InputStreamReader(str));

                js.beginObject();
                while (js.hasNext() && !js.nextName().equals("webcams")) {
                    js.skipValue();
                }
                js.beginObject();
                while (js.hasNext() && !js.nextName().equals("webcam")) {
                    js.skipValue();
                }
                js.beginArray();
                js.beginObject();



                URL pru = null;
                while (js.hasNext()) {
                    String name = js.nextName();
                    if (name.equals("preview_url")) {
                        pru = new URL(js.nextString());
                        Log.d(CityCamActivity.TAG, "preview_url found");
                        break;
                    }
                    js.skipValue();
                }

                if (pru == null) {
                    Log.d(CityCamActivity.TAG, "preview_url NOT found");
                    return null;

                }
                else {
                    return BitmapFactory.decodeStream(pru.openStream());
                }
            }  catch (Exception e)
                {
                    return null;
                }
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap == null) {
                Log.d(CityCamActivity.TAG, "Download unsuccessful");
            } else {
                Log.d(CityCamActivity.TAG, "Download successful");
                activity.progressView.setVisibility(View.INVISIBLE);
                activity.camImageView.setVisibility(View.VISIBLE);
                activity.camImageView.setImageBitmap(bitmap);
                activity.bitmap = bitmap;
            }
        }

        void attachActivity(CityCamActivity activity) {

            this.activity = activity;
        }

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




        if (savedInstanceState != null) {
            cams = (Cams) getLastCustomNonConfigurationInstance();
        }
        if (cams == null) {
            cams = new Cams(this);
            cams.execute(city);
        } else {
            cams.attachActivity(this);
        }






        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
    }



    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return cams;
    }



    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        Log.d(TAG, "going to be rotated");
        state.putParcelable("bit", bitmap);
    }
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        Log.d(TAG, "restoring after rotation");
        bitmap = state.getParcelable("bit");
        if (bitmap != null) {
            Log.d(TAG, "bitmap is NOT null");
            camImageView.setVisibility(View.VISIBLE);
            camImageView.setImageBitmap(bitmap);
            progressView.setVisibility(View.INVISIBLE);
        }
    }



    private static final String TAG = "CityCam";
}
