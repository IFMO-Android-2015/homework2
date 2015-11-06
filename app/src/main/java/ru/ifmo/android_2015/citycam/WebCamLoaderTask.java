package ru.ifmo.android_2015.citycam;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

import ru.ifmo.android_2015.citycam.api.RestClient;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.WebCam;
import ru.ifmo.android_2015.citycam.model.WebCamsResult;

/**
 * @author creed
 * @date 06.11.15
 */
public class WebCamLoaderTask extends AsyncTask<City, Integer, Bitmap> {
    private ImageView camImageView;
    private ProgressBar progressView;
    private TextView updated;

    private Context context;

    private long t;
    private boolean isGood;

    public WebCamLoaderTask(Context context) {
        this.context = context;
        this.camImageView = (ImageView)((Activity)context).findViewById(R.id.cam_image);
        this.progressView = (ProgressBar)((Activity)context).findViewById(R.id.progress);
        this.updated = (TextView)((Activity)context).findViewById(R.id.updated);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressView.setVisibility(View.VISIBLE);
        isGood = false;
    }

    @Override
    protected Bitmap doInBackground(City... params) {
        City city = params[0];
        WebCamsResult result = RestClient.webCams(city.latitude, city.longitude);
        if (result == null) {
            Log.e("CityCam", "result is null");
            return null;
        } else {
            Bitmap bitmap;
            if (!result.getWebcams().getWebcam().isEmpty()) {
                WebCam cam = result.getWebcams().getWebcam().get(0);
                String url = cam.getPreviewUrl();
                t = cam.getLastUpdate();
                bitmap = getBitmapFromURL(url);
                isGood = true;
            } else {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            }
            return bitmap;
        }
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        camImageView.setImageBitmap(bitmap);
        progressView.setVisibility(View.GONE);
        if (isGood) {
            PrettyTime p = new PrettyTime(new Locale("ru"));
            updated.setText("Последнее обновление: " + p.format(new Date(t * 1000)));
        }
    }
}
