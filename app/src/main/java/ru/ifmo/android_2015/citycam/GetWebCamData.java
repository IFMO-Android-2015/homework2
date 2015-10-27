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
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.WebCam;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Created by hp on 26.10.2015.
 */
public class GetWebCamData extends AsyncTask<City, WebCam, Bitmap> {

    public String progress;
    private final String TAG = "GetWebcamData";
    CityCamActivity activity;
    public GetWebCamData(CityCamActivity activity) {
        this.activity = activity;
        this.progress = "DataDownloading";
    }

    public void attachActivity(CityCamActivity activity) {
        Log.w(TAG, "Activity Attached");
        this.activity = activity;
    }
    protected Bitmap doInBackground(City... city) {
        try {
            WebCam webCam;
            webCam = getWebCamData(city[0]);
            progress = "PictureDownloading";
            publishProgress(webCam);
            Bitmap pic = null;
            if (webCam != null && webCam.picURL != null) {
                pic = getPicture(webCam.picURL);
            }
            progress = "DownloadFinished";
            return pic;
        } catch (IOException e) {
            Log.i("",e.toString());
            progress = "ERROR";
            return null;
        }
    }

    protected void onProgressUpdate(WebCam... webCams) {
        WebCam webCam = webCams[0];
        if (webCam == null) {
            progress = "ERROR";
            Toast toast = Toast.makeText(activity, "Network error! Please check your network connection.", Toast.LENGTH_LONG);
            toast.show();
            activity.finish();
        } else {
            if (webCam.picURL == null) {
                progress = "ERROR";
                Toast toast = Toast.makeText(activity, "No cameras in this location!", Toast.LENGTH_LONG);
                toast.show();
                activity.finish();
            } else {
                TextView date = (TextView) activity.findViewById(R.id.last_update);
                TextView viewCount = (TextView) activity.findViewById(R.id.ViewCount);
                viewCount.append(Long.toString(webCam.viewCount));
                viewCount.setVisibility(View.VISIBLE);
                TextView averageRating = (TextView) activity.findViewById(R.id.AverageRating);
                averageRating.append(Double.toString(webCam.averageRating));
                averageRating.setVisibility(View.VISIBLE);
                TextView ratingCount = (TextView) activity.findViewById(R.id.RatingsCount);
                ratingCount.append(Long.toString(webCam.ratingCount));
                ratingCount.setVisibility(View.VISIBLE);
                DateFormat df = DateFormat.getTimeInstance();
                date.append(df.format(webCam.date));
                date.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void onPostExecute(Bitmap result) {
        if (result != null) {
            ImageView picture = (ImageView) activity.findViewById(R.id.cam_image);
            ProgressBar progressView = (ProgressBar) activity.findViewById(R.id.progress);
            picture.setImageBitmap(result);
            progressView.setVisibility(View.INVISIBLE);
        }
    }

    private WebCam getWebCamData(City city) throws IOException {
        WebCam webCam = new WebCam();
        HttpURLConnection conn = (HttpURLConnection) Webcams.createNearbyUrl(city.latitude, city.longitude).openConnection();
        InputStream in = null;
        try {
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new FileNotFoundException("Unexpected HTTP response: " + responseCode
                        + ", " + conn.getResponseMessage());
            }
            in = conn.getInputStream();
            JsonReader jsonReader = new JsonReader(new InputStreamReader(in));
            jsonReader.beginObject();
            while (!jsonReader.nextName().equals("webcams")) {
                jsonReader.skipValue();
            }
            jsonReader.beginObject();
            while (!jsonReader.nextName().equals("webcam")) {
                jsonReader.skipValue();
            }
            jsonReader.beginArray();
            if (!jsonReader.hasNext()) {
                return webCam;
            }
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                switch (name) {
                    case "preview_url" : {
                        webCam.picURL = jsonReader.nextString();
                    } break;
                    case "last_update" : {
                        webCam.date = new Date(jsonReader.nextLong() * 1000);
                    } break;
                    case "view_count" : {
                        webCam.viewCount = jsonReader.nextLong();
                    } break;
                    case "rating_avg" : {
                        webCam.averageRating = jsonReader.nextDouble();
                    } break;
                    case "rating_count" : {
                        webCam.ratingCount = jsonReader.nextLong();
                    } break;
                    default: {
                        jsonReader.skipValue();
                    }
                }
            }
            jsonReader.close();
            return webCam;
        } catch (IOException e1){
            Log.i("", e1.toString());
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            return null;
        }
    }

    private Bitmap getPicture(String picURL) throws  IOException{
        URL url = new URL(picURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream in = null;
        try {

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new FileNotFoundException("Unexpected HTTP response: " + responseCode
                        + ", " + conn.getResponseMessage());
            }

            in = conn.getInputStream();
            Bitmap picture = BitmapFactory.decodeStream(in);
            in.close();
            return picture;
        } catch (IOException e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
