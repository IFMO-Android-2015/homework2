package ru.ifmo.android_2015.citycam;

import android.content.Context;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

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

    static public City city;

    Bitmap image;
    ImageView camImageView;
    TextView textView;
    ProgressBar progressView;

    //
    private static final String DOWNLOAD_URL = "http://files3.vunivere.ru/workbase/00/01/48/09/images/image009.gif";
    private BackgroundDownloader downloader;
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
        textView = (TextView) findViewById(R.id.textView);
        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        if (Data.city != null) {
            if (city.name.equals(Data.city.name)) {
                if (Data.image != null) {
                    image = Data.image;
                    camImageView.setImageBitmap(Data.image);
                    textView.setText(Data.title);
                    progressView.setVisibility(View.INVISIBLE);
                }
            } else {
                download(savedInstanceState);
            }
        } else {
            download(savedInstanceState);
        }


    }

    void download(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            downloader = (BackgroundDownloader) getLastCustomNonConfigurationInstance();
        }
        if (downloader == null) {
            downloader = new BackgroundDownloader(this);
            downloader.execute();
        } else {
            downloader.attachActivity(this);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public Object onRetainCustomNonConfigurationInstance() {
        return downloader;
    }



    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        Data.image = image;
        Data.title = textView.getText().toString();
        Data.city = city;
//        if (image != null) {
//            ByteArrayOutputStream s = new ByteArrayOutputStream(image.getWidth() * image.getHeight());
//            image.compress(Bitmap.CompressFormat.JPEG, 100, s);
//            bundle.putByteArray("image", s.toByteArray());
//            bundle.putString("title", textView.getText().toString());
//
//        }
    }


    private static final String TAG = "CityCam";
}

class Data {
    public static City city;
    public static Bitmap image;
    public static  String title;
}