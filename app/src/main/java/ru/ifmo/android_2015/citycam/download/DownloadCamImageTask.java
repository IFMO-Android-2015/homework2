package ru.ifmo.android_2015.citycam.download;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

public class DownloadCamImageTask extends AsyncTask<City, Void, Webcam> {
    private static final String LOGTAG = "Downloading";
    private Activity activity;
    private Webcam webcam = null;

    public enum Progress {Error, GettingInfo, GettingImage, Done}
    private Progress progress;

    public Progress getProgress() {
        return progress;
    }

    public DownloadCamImageTask(Activity activity) {
        this.activity = activity;
        progress = Progress.GettingInfo;
    }

    public void attachActivity(Activity activity) {
        this.activity = activity;
        updateView(activity);
    }

    private void updateView(Activity activity) {
        if (activity != null) {
            if (progress == Progress.GettingInfo || progress == Progress.GettingImage) {
                activity.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                activity.findViewById(R.id.cam_name).setVisibility(View.INVISIBLE);
            } else {
                activity.findViewById(R.id.cam_image).setVisibility(View.VISIBLE);
                activity.findViewById(R.id.cam_name).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected Webcam doInBackground(City... params) {
        try {
            progress = Progress.GettingInfo;
            webcam = getInfoAboutWebcam(params[0]);

            if (webcam != null) {
                progress = Progress.GettingImage;
                webcam.setImage(getBitmap(webcam));
                progress = Progress.Done;
            } else {
                progress = Progress.Error;
            }

            return webcam;
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Webcam webcam) {
        ImageView view = (ImageView) activity.findViewById(R.id.cam_image);
        TextView title = (TextView) activity.findViewById(R.id.cam_title);
        TextView coordinates = (TextView) activity.findViewById(R.id.coordinates);
        TextView lat = (TextView) activity.findViewById(R.id.latitude);
        TextView lon = (TextView) activity.findViewById(R.id.longitude);
        activity.findViewById(R.id.progress).setVisibility(View.INVISIBLE);

        if (webcam != null && progress != Progress.Error){
            view.setImageBitmap(webcam.getImage());
            activity.findViewById(R.id.cam_name).setVisibility(View.VISIBLE);
            title.setText(webcam.getCamTitle());
            coordinates.setVisibility(View.VISIBLE);
            activity.findViewById(R.id.latitude_title).setVisibility(View.VISIBLE);
            lat.setText(webcam.getLatitude().toString());
            activity.findViewById(R.id.longitude_title).setVisibility(View.VISIBLE);
            lon.setText(webcam.getLongitude().toString());
        } else {
            view.setImageResource(R.drawable.error);
            view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        view.setVisibility(View.VISIBLE);
    }

    private Webcam getInfoAboutWebcam(City city) throws IOException {
        URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream is = connection.getInputStream();

        List webcams = readJsonStream(is);

        if (is != null) {
            is.close();
        }
        connection.disconnect();

        //TODO: User should choose a cam
        if (!webcams.isEmpty()) {
            return (Webcam) webcams.get(0);
        } else {
            return null;
        }
    }

    private List readJsonStream(InputStream is) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(is));

        try {
            return readWebcams(reader);
        } finally {
            reader.close();
        }
    }

    private List readWebcams(JsonReader reader) throws IOException {
        List result = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("webcams")) {
                result =  readWebcamArray(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return result;
    }

    private List readWebcamArray(JsonReader reader) throws IOException {
        List result = new ArrayList();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("webcam")) {
                reader.beginArray();
                while (reader.hasNext()) {
                    result.add(readWebcam(reader));
                }
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return result;
    }

    private Webcam readWebcam(JsonReader reader) throws IOException {
        Webcam res = new Webcam();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "daylight_preview_url":
                    res.setPreviewUrl(reader.nextString());
                    break;
                case "title":
                    res.setCamTitle(reader.nextString());
                    break;
                case "latitude":
                    res.setLatitude(reader.nextDouble());
                    break;
                case "longitude":
                    res.setLongitude(reader.nextDouble());
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        return res;
    }


    private Bitmap getBitmap(Webcam webcam) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (new URL(webcam.getPreviewUrl())).openConnection();
        InputStream is = connection.getInputStream();

        Bitmap bitmap = BitmapFactory.decodeStream(is);
        is.close();
        connection.disconnect();
        return bitmap;
    }
}
