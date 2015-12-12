package ru.ifmo.android_2015.citycam.download;

import android.app.Activity;
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

import ru.ifmo.android_2015.citycam.R;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcam;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

public class DownloadWebcamImageTask extends AsyncTask<City, Void, Webcam> {
    private Activity activity;

    public enum Status {Error, GettingInfo, GettingImage, Finished}

    private Status status;

    public Status getTaskStatus() {
        return status;
    }

    public DownloadWebcamImageTask(Activity activity) {
        this.activity = activity;
        status = Status.GettingInfo;
    }

    public void attachActivity(Activity activity) {
        this.activity = activity;
        updateView(activity);
    }

    private void updateView(Activity activity) {
        if (activity != null) {
            if (status == Status.GettingInfo || status == Status.GettingImage) {
                activity.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            } else {
                activity.findViewById(R.id.cam_image).setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    protected Webcam doInBackground(City... params) {
        Webcam webcam;
        try {
            status = Status.GettingInfo;
            webcam = getWebcamInfo(params[0]);

            if (webcam != null) {
                status = Status.GettingImage;
                webcam.setImage(decodeImage(webcam));
                if (webcam.getImage() == null) {
                    status = Status.Error;
                } else {
                    status = Status.Finished;
                }
            } else {
                status = Status.Error;
            }
            return webcam;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Webcam webcam) {
        ImageView camImageView = (ImageView) activity.findViewById(R.id.cam_image);
        ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.progress);
        TextView title = (TextView) activity.findViewById(R.id.title);
        TextView info = (TextView) activity.findViewById(R.id.info);
        progressBar.setVisibility(View.INVISIBLE);

        if (webcam != null && status != Status.Error) {
            camImageView.setImageBitmap(webcam.getImage());
            title.setText(webcam.getTitle());
            info.setText("Широта: " + webcam.getLatitude() + "\nДолгота: " + webcam.getLongitude());
        } else {
            title.setText("Нет доступных камер");
            camImageView.setImageResource(R.drawable.error);
            camImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        camImageView.setVisibility(View.VISIBLE);
    }

    private Bitmap decodeImage(Webcam webcam) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (new URL(webcam.getPreviewUrl())).openConnection();
        InputStream is = connection.getInputStream();
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        is.close();
        connection.disconnect();

        return bitmap;
    }

    private Webcam getWebcamInfo(City city) throws IOException {
        URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream is = connection.getInputStream();
        List<Webcam> webcams = null;
        try {
            webcams = readJsonStream(is);
        } finally {
            if (is != null) {
                is.close();
            }
            connection.disconnect();
        }
        if (webcams == null || webcams.isEmpty()) {
            return null;
        } else {
            return (Webcam) webcams.get(0);
        }
    }

    private List<Webcam> readJsonStream(InputStream is) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(is));
        List<Webcam> list = readWebcams(reader);
        reader.close();
        return list;
    }

    private List<Webcam> readWebcams(JsonReader reader) throws IOException {
        List<Webcam> list = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("webcams")) {
                list = readWebcamArray(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return list;
    }

    private List<Webcam> readWebcamArray(JsonReader reader) throws IOException {
        List<Webcam> list = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("webcam")) {
                reader.beginArray();
                while (reader.hasNext()) {
                    list.add(readWebcam(reader));
                }
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return list;
    }

    private Webcam readWebcam(JsonReader reader) throws IOException {
        Webcam result = new Webcam();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "daylight_preview_url":
                    result.setPreviewUrl(reader.nextString());
                    break;
                case "title":
                    result.setTitle(reader.nextString());
                    break;
                case "latitude":
                    result.setLatitude(reader.nextDouble());
                    break;
                case "longitude":
                    result.setLongitude(reader.nextDouble());
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        return result;
    }

    private static final String TAG = "Downloading";
}
