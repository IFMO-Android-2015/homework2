package ru.ifmo.android_2015.citycam;

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

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.CamInfo;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

public class LoadingAsyncTask extends AsyncTask<City, CamInfo, CamInfo> {

    private static final String LOGTAG = "Downloading";
    private Activity activity;
    private DownloadState state;
    ImageView view;
    TextView title;
    TextView lat;
    TextView lon;
    ProgressBar pr;

    public DownloadState getState() {
        return state;
    }

    enum DownloadState {
        DOWNLOADING(R.string.downloading),
        DONE(R.string.done),
        ERROR(R.string.error),
        NOCAMS(R.string.no_cameras);

        // ID строкового ресурса для заголовка окна прогресса
        final int titleResId;

        DownloadState(int titleResId) {
            this.titleResId = titleResId;
        }
    }

    public LoadingAsyncTask(Activity activity) {
        this.activity = activity;
        view = (ImageView) activity.findViewById(R.id.cam_image);
        title = (TextView) activity.findViewById(R.id.cam_title);
        lat = (TextView) activity.findViewById(R.id.latitude);
        lon = (TextView) activity.findViewById(R.id.longitude);
        pr  = (ProgressBar) activity.findViewById(R.id.progress);
    }

    public void attachActivity(Activity activity) {
        this.activity = activity;
        updateView(activity);
    }

    private void updateView(Activity activity) {
        if (activity != null) {
            if (state == DownloadState.DOWNLOADING) {
                activity.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            } else {
                activity.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected CamInfo doInBackground(City... params) {
        try {
            state = DownloadState.DOWNLOADING;
            CamInfo camInfo = getInfo(params[0]);

            if (camInfo != null) {
                camInfo.setImage(getBitmap(camInfo));
                if (camInfo.getImage() == null) {
                    state = DownloadState.NOCAMS;
                } else {
                    state = DownloadState.DONE;
                }
            } else {
                state = DownloadState.ERROR;
            }

            return camInfo;
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(CamInfo webcam) {
        view = (ImageView) activity.findViewById(R.id.cam_image);
        title = (TextView) activity.findViewById(R.id.cam_title);
        lat = (TextView) activity.findViewById(R.id.latitude);
        lon = (TextView) activity.findViewById(R.id.longitude);
        pr  = (ProgressBar) activity.findViewById(R.id.progress);
        pr.setVisibility(View.INVISIBLE);
        if (webcam == null || state == DownloadState.NOCAMS) {
            view.setImageResource(R.drawable.nocam);
            title.setText(R.string.no_cameras);
            return;
        }
        if(state == DownloadState.ERROR){
            view.setImageResource(R.drawable.er);
            title.setText(R.string.error);
            return;
        }
        view.setImageBitmap(webcam.getImage());
        title.setText(webcam.getCamTitle());
        lat.setText(webcam.getLatitude().toString());
        lon.setText(webcam.getLongitude().toString());
    }

    private CamInfo getInfo(City city) throws IOException {
        URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream in = connection.getInputStream();
            try {
                JsonReader reader = new JsonReader(new InputStreamReader(in));
                CamInfo res = readWebs(reader);
                reader.close();
                return res;
            }
            finally {
                if (in != null) {
                  in.close();
                }
                connection.disconnect();
            }

    }

    private CamInfo readWebs(JsonReader reader) throws IOException {
        CamInfo result = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("webcams")) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String name1 = reader.nextName();
                    if (name1.equals("webcam")) {
                        reader.beginArray();
                        while (reader.hasNext()) {
                            result = takeData(reader);
                        }
                        reader.endArray();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return result;
    }

    private CamInfo takeData(JsonReader reader) throws IOException {
        CamInfo res = new CamInfo();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "preview_url":
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

    private Bitmap getBitmap(CamInfo camInfo) throws IOException {
        HttpURLConnection connection = null;
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            connection = (HttpURLConnection) (new URL(camInfo.getPreviewUrl())).openConnection();
            is = connection.getInputStream();
//            Log.w("asdf", (new URL(camInfo.getPreviewUrl())).toString());
            bitmap = BitmapFactory.decodeStream(is);
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
        return  bitmap;
    }
}
