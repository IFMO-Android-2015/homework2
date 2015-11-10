package ru.ifmo.android_2015.citycam.model;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ru.ifmo.android_2015.citycam.CityCamActivity;
import ru.ifmo.android_2015.citycam.R;
import ru.ifmo.android_2015.citycam.webcams.Webcam;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Created by kurkin on 01.11.15.
 */
public class DownloadWebcamImage extends AsyncTask<City, Void, Webcam> {

    private CityCamActivity activity;
    private static final String TAG = "Download";

    public enum State {ERROR, FINISHED, INPROGRESS}

    private State state;

    public State getState() {
        return state;
    }


    public DownloadWebcamImage(CityCamActivity activity) {
        this.activity = activity;
    }

    public void attachActivity(CityCamActivity activity) {
        this.activity = activity;
        updateView();
    }

    void updateView() {
        if (activity != null) {
            if (state == State.ERROR) {
                activity.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                ((ImageView) activity.findViewById(R.id.cam_image)).
                        setImageDrawable(activity.getResources().getDrawable(R.drawable.noimage));
                ((TextView) activity.findViewById(R.id.title)).setText("нет доступных камер");
            }
            if (state == State.INPROGRESS) {
                ((TextView) activity.findViewById(R.id.title)).setText("загрузка");
            }
        }
    }

    @Override
    protected Webcam doInBackground(City... params) {
        state = State.INPROGRESS;
        try {
            Webcam webcam = getFirstWebcam(params[0]);
            if (webcam != null) {
                webcam.setImage(DownloadImage(webcam.getPreview_url()));
                state = State.FINISHED;
            } else {
                Log.e(TAG, "no webcams in city");
                state = State.ERROR;
            }
            return webcam;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            state = State.ERROR;
            return null;
        }
    }

    private Bitmap DownloadImage(URL preview_url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) preview_url.openConnection();
        connection.connect();
        InputStream is = connection.getInputStream();
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        is.close();
        connection.disconnect();
        return bitmap;
    }

    @Override
    protected void onPostExecute(Webcam webcam) {
        ImageView camImageView = (ImageView) activity.findViewById(R.id.cam_image);
        ProgressBar progressView = (ProgressBar) activity.findViewById(R.id.progress);
        TextView title = (TextView) activity.findViewById(R.id.title);
        TextView info = (TextView) activity.findViewById(R.id.info);
        progressView.setVisibility(View.INVISIBLE);
        if (webcam != null) {
            camImageView.setImageBitmap(webcam.getImage());
            title.setText(webcam.getTitle());
            info.setText("Долгота: " + webcam.getLongitude() + " Широта: " + webcam.getLatitude());
        } else {
            title.setText("нет доступных камер");
            camImageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.noimage));
        }
    }

    private Webcam getFirstWebcam(City city) throws IOException {
        URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        InputStream is = conn.getInputStream();
        List<Webcam> webcams = null;
        JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
        try {
            reader.beginObject();
            switch (reader.nextName()) {
                case "webcams": webcams = parseWebcams(reader); break;
                default: skipValue(); break;
            }
            reader.endObject();
        } finally {
            is.close();
            conn.disconnect();
        }
        if (webcams == null)
            return null;
        return webcams.get(0);
    }


    private List<Webcam> readWebcams(JsonReader reader) throws IOException {
        reader.beginObject();
        List<Webcam> webcams = new ArrayList<>();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "webcam": {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        webcams.add(readWebcam(reader));
                    }
                    reader.endArray();
                    break;
                }
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        return webcams;
    }

    private Webcam readWebcam(JsonReader reader) throws IOException {
        reader.beginObject();
        Webcam webcam = new Webcam();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "title": {
                    webcam.setTitle(reader.nextString());
                    break;
                }
                case "longitude": {
                    webcam.setLongitude(reader.nextDouble());
                    break;
                }
                case "latitude": {
                    webcam.setLatitude(reader.nextDouble());
                    break;
                }
                case "preview_url": {
                    webcam.setPreview_url(new URL(reader.nextString()));
                    break;
                }
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        return webcam;
    }
}

