package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.WebCam;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Created by dns on 26.12.2015.
 */
public class GetWebcamImageAsyncTask extends AsyncTask<City, WebCam, Bitmap>
{
    private static final String TAG="JSON_Request";
    private CityCamActivity activity;
    public enum Progress {Downloading, Error, LoadedData, LoadedImage}
    private Progress progress;

    public GetWebcamImageAsyncTask(CityCamActivity activity) {
        this.activity = activity;
        this.progress = Progress.Downloading;
    }

    void attach(CityCamActivity activity) {
        this.activity = activity;
    }

    public Progress getProgress() {
        return this.progress;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        ImageView view = (ImageView) activity.findViewById(R.id.camImage);
        if (bitmap == null) {
            view.setImageResource(R.drawable.bad);
            this.progress = Progress.Error;
        } else {
            view.setImageBitmap(bitmap);
            this.progress = Progress.LoadedImage;
        }

        activity.findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    @Override
    protected Bitmap doInBackground(City... params) {
        WebCam cam = loadCam(params[0]);
        publishProgress(cam);
        return loadBitmap(cam != null && cam.exists() ? cam.getURL() : null);
    }

    @Override
    protected void onProgressUpdate(WebCam... cams) {
        WebCam cam = cams[0];
        TextView text = (TextView) activity.findViewById(R.id.titleText);
        this.progress = Progress.Error;
        if (cam == null) {
            text.setText("Network error");
        } else if (!cam.exists()) {
            text.setText("No camera found nearby");
        } else {
            text.setText(cam.getTitle());
            RatingBar rating = (RatingBar) activity.findViewById(R.id.ratingBar);
            rating.setRating((float) cam.getRating());
            rating.setVisibility(View.VISIBLE);
            TextView updated = (TextView) activity.findViewById(R.id.lastUpdatedText);
            DateFormat date = new SimpleDateFormat("HH:mm, dd.MM.yyyy", Locale.getDefault());
            updated.append("\n" + date.format(cam.getLastUpdate()));
            updated.setVisibility(View.VISIBLE);
            this.progress = Progress.LoadedData;
        }
    }

    private WebCam loadCam(City city) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL request = Webcams.createNearbyUrl(city.latitude, city.longitude);
            connection = (HttpURLConnection) request.openConnection();
            inputStream = connection.getInputStream();
            if (connection.getResponseCode() != 200) {
                throw new IOException("Bad response from server: " + connection.getResponseMessage());
            }

            JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
            reader.beginObject();
            while (!reader.nextName().equals("webcams")) {
                reader.skipValue();
            } //Going to array of cams
            reader.beginObject();
            while (!reader.nextName().equals("webcam")) {
                reader.skipValue();
            } //Taking the first one
            reader.beginArray();
            if (!reader.hasNext()) return new WebCam(); //If there is no cameras nearby
            reader.beginObject();
            String preview = null;
            String title = null;
            double rating = 0;
            long lastUpdated = 0L;
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch(name) {
                    case "preview_url" : {
                        preview = reader.nextString();
                        break;
                    }
                    case "title" : {
                        title = reader.nextString();
                        break;
                    }
                    case "rating_avg" : {
                        rating = reader.nextDouble();
                        break;
                    }
                    case "last_update" : {
                        lastUpdated = reader.nextLong();
                        break;
                    }
                    default :
                        reader.skipValue();
                }
            }
            reader.close();
            return new WebCam (preview == null ? null : new URL(preview), title, new Date(lastUpdated * 1000), rating);

        } catch(IOException | IllegalStateException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch(IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private Bitmap loadBitmap(URL url) {
        if (url == null) return null;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() != 200) {
                throw new IOException("Bad response from server: " + connection.getResponseMessage());
            }
            inputStream = connection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch(IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            if (connection != null)
                connection.disconnect();
        }
    }
}
