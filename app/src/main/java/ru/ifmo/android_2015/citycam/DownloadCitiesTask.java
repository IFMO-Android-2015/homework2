package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.WebcamInfo;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

public class DownloadCitiesTask extends AsyncTask<City, WebcamInfo, Bitmap> {
    private CityCamActivity cityCamActivity;
    private static final String TAG = "DownloadCitiesTask";
    private WebcamInfo webcam;
    public Bitmap bitmap;
    private StringBuilder message;
    private TextView textView;
    private ImageView view;
    private ProgressBar progressView;
    boolean error = false;

    public DownloadCitiesTask(CityCamActivity cityCamActivity) {
        this.cityCamActivity = cityCamActivity;
    }

    void attActivity(CityCamActivity cityCamActivity) {
        this.cityCamActivity = cityCamActivity;
        textView = (TextView) cityCamActivity.findViewById(R.id.textView);
        progressView = (ProgressBar)cityCamActivity.findViewById(R.id.progress);
        if (message != null) {
            textView.setText(message.toString());
            progressView.setVisibility(View.GONE);
            view = (ImageView) cityCamActivity.findViewById(R.id.cam_image);
            view.setVisibility(View.VISIBLE);
            view.setImageBitmap(bitmap);
        } else if (error) {
            textView.setText(message.toString());
        } else {
            publishProgress(webcam);
        }
    }

    public Bitmap getImage(URL url) throws IOException {
        if (url == null) {
            error = true;
            return null;
        }
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            int status = connection.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                    is = connection.getInputStream();
                    Bitmap bit = BitmapFactory.decodeStream(is);
                    return bit;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                is.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    public WebcamInfo sendRequest(double latitude, double longitude) {
        HttpURLConnection connection = null;
        try {
            URL url = Webcams.createNearbyUrl(latitude, longitude);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            int status = connection.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                    JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
                    String camurl = null;
                    String webname = null;
                    String city = null;
                    String country = null;
                    reader.beginObject();
                    while (!reader.nextName().equals("webcams")) {
                        reader.skipValue();
                    }
                    reader.beginObject();
                    while (!reader.nextName().equals("webcam")) {
                        reader.skipValue();
                    }
                    reader.beginArray();
                    if (!reader.hasNext()) {
                        return null;
                    }
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        if (name.equals("preview_url")) {
                            camurl = reader.nextString();
                        } else if (name.equals("title")) {
                            webname = reader.nextString();
                        } else if (name.equals("city")) {
                            city = reader.nextString();
                        } else if (name.equals("country")) {
                            country = reader.nextString();
                        } else {
                            reader.skipValue();
                        }
                    }
                    reader.close();
                    return new WebcamInfo(camurl, webname, city, country);
            }

        } catch (MalformedURLException e) {
            System.out.println(e.toString());
            return null;
        } catch (IOException e) {
            error = true;
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected Bitmap doInBackground(City... params) {
        webcam = sendRequest(cityCamActivity.getCity().latitude, cityCamActivity.getCity().longitude);
        publishProgress(webcam);
        if (webcam == null) {
            return null;
        }
        try {
            bitmap = getImage(new URL(webcam.cameraUrl));
            return bitmap;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        view = (ImageView) cityCamActivity.findViewById(R.id.cam_image);
        if (bitmap == null) {
            //error
        } else {
            view.setImageBitmap(bitmap);
        }
        textView = (TextView) cityCamActivity.findViewById(R.id.textView);
        message = new StringBuilder();
        if (error) {
            message.append("No internet connection");
        } else if (webcam == null) {
            message.append("No webcamera here");
        } else {
            message.append(webcam.city);
            message.append(" (");
            message.append(webcam.country);
            message.append(") : ");
            message.append(webcam.name);
        }
        textView.setText(message.toString());
        cityCamActivity.findViewById(R.id.progress).setVisibility(View.GONE);
    }
}
