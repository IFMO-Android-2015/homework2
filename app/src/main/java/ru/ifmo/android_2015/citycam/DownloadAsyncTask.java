package ru.ifmo.android_2015.citycam;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
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

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

import static ru.ifmo.android_2015.citycam.R.drawable.ochenzhal;

public class DownloadAsyncTask extends AsyncTask<City, Void, DownloadAsyncTask.WebcamInfo> {
    private static final String LOGTAG = "Downloading";
    private Activity activity;
    private WebcamInfo webcam = null;
    public Integer progress = 0;//done - 0, getting info - 1, getting image - 2, error - 3
    public class WebcamInfo {
        public String camTitle, camURL;
        public Double latitude, longitude;
        public Bitmap image;
    }

    public DownloadAsyncTask(Activity activity) {
        this.activity = activity;
        progress = 1;
    }

    public void updateView(Activity activity) {
        this.activity = activity;
        if (activity != null) {
            if (progress == 1 || progress == 2) {
                activity.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                activity.findViewById(R.id.cam_name).setVisibility(View.INVISIBLE);
            } else {
                activity.findViewById(R.id.cam_image).setVisibility(View.VISIBLE);
                activity.findViewById(R.id.cam_name).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected WebcamInfo doInBackground(City... params) {
        try {
            progress = 1;
            webcam = getInfoAboutWebcam(params[0]);
            if (webcam != null) {
                progress = 2;
                InputStream is = null;
                HttpURLConnection connection = null;
                Bitmap bitmap = null;
                try {
                    connection = (HttpURLConnection) (new URL(webcam.camURL)).openConnection();
                    is = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                } catch (Exception e) {
                    Log.e(LOGTAG, e.getMessage());
                } finally {
                    if (is != null)
                        is.close();
                    if (connection != null)
                        connection.disconnect();
                }
                if (bitmap != null) {
                    webcam.image = bitmap;
                    progress = 0;
                } else
                    progress = 3;
            } else {
                progress = 3;
            }
            return webcam;
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(WebcamInfo webcam) {
        ImageView view = (ImageView) activity.findViewById(R.id.cam_image);
        TextView title = (TextView) activity.findViewById(R.id.cam_title);
        TextView coordinates = (TextView) activity.findViewById(R.id.coordinates);
        TextView lat = (TextView) activity.findViewById(R.id.latitude);
        TextView lon = (TextView) activity.findViewById(R.id.longitude);
        activity.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        if (webcam != null && progress != 3) {
            activity.findViewById(R.id.cam_name).setVisibility(View.VISIBLE);
            view.setImageBitmap(webcam.image);
            title.setText(webcam.camTitle);
            coordinates.setVisibility(View.VISIBLE);
            activity.findViewById(R.id.latitude_title).setVisibility(View.VISIBLE);
            lat.setText(webcam.latitude.toString());
            activity.findViewById(R.id.longitude_title).setVisibility(View.VISIBLE);
            lon.setText(webcam.longitude.toString());
        } else {
            view.setImageResource(ochenzhal);
            view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
        view.setVisibility(View.VISIBLE);
    }

    private WebcamInfo getInfoAboutWebcam(City city) throws IOException {
        URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream is = connection.getInputStream();
        List webcams = readJsonStream(is);
        if (is != null)
            is.close();
        connection.disconnect();
        if (!webcams.isEmpty())
            return (WebcamInfo) webcams.get(0);
        else
            return null;
    }
    //next methods - JSON parser
    //according to http://developer.android.com/intl/ru/reference/android/util/JsonReader.html
    //             http://ru.webcams.travel/developers/api/wct.webcams.get_details
    private List readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readWebcams(reader);
        } finally {
            reader.close();
        }
    }

    private List readWebcams(JsonReader reader) throws IOException {
        List messages = new ArrayList();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("webcams")) {
                messages = readWebcamArray(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return messages;
    }

    private List readWebcamArray(JsonReader reader) throws IOException {
        List result = new ArrayList();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("webcam")) {
                reader.beginArray();
                while (reader.hasNext())
                    result.add(readWebcam(reader));
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return result;
    }

    private WebcamInfo readWebcam(JsonReader reader) throws IOException {
        WebcamInfo res = new WebcamInfo();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "daylight_preview_url":
                    res.camURL = reader.nextString();
                    break;
                case "title":
                    res.camTitle = reader.nextString();
                    break;
                case "latitude":
                    res.latitude = reader.nextDouble();
                    break;
                case "longitude":
                    res.longitude = reader.nextDouble();
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        return res;
    }
}
