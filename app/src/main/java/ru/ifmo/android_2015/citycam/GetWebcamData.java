package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.NoSuchElementException;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Created by Lenovo on 02.11.2015.
 */
class Info {
    String where;
    Bitmap pic;
    String picURL;
}

public class GetWebcamData extends AsyncTask<City, Info, Bitmap> {
    private static final String TAG = "Async";

    ;
    public Status currentState;
    private CityCamActivity attached;
    private Info inf;


    public GetWebcamData(CityCamActivity parent) {
        attached = parent;
        currentState = Status.STARTED;
    }

    public void newParent(CityCamActivity parent) {
        Log.i(TAG, "Activity REBUILD");
        attached = parent;
    }

    @Override
    protected Bitmap doInBackground(City... params) {
        try {
             inf = new Info();
            if (parseJSON(inf, Webcams.createNearbyUrl(params[0].latitude, params[0].longitude)) != 0) {
                currentState = Status.FAIL;
                return null;
            }
            publishProgress(inf);
            if (getImageByURL(inf.picURL) != 0) {
                currentState = Status.FAIL;
                return null;
            }
            currentState = Status.ALLCLEAR;
            publishProgress(inf);
            return inf.pic;
        } catch (IOException e) {
            Log.i(TAG, "Cannot even create URL");
        }
        return inf.pic;
    }
    private void Toasty(final String in,final int duration){
        attached.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(attached, in, duration).show();
            }
        });
    }
    @Override
    protected void onProgressUpdate(Info... params) {
        attached.setTitle(params[0].where);
    }

    private int parseJSON(Info to, URL url) {
        int result = -1;
        HttpURLConnection conn = null;
        InputStream bis = null;
        JsonReader reader = null;
        try {
            Log.i(TAG, "Getting JSON");
            conn = (HttpURLConnection) url.openConnection();
            int resp = conn.getResponseCode();
            if (resp != 200) {
                Log.i(TAG, "HTTP Response is not OK");
                 Toasty("Сервер веб-камер недоступен", Toast.LENGTH_LONG);
                attached.finish();
                result = 1;
                throw new ConnectException("Connection lost");
            }
            bis = conn.getInputStream();
            reader = new JsonReader(new InputStreamReader(bis));
            Log.i(TAG, url.toString());
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("webcams")) {
                    reader.beginObject();reader.skipValue();
                    Long count = reader.nextLong();
                    if (count == 0) {
                        Toasty( "Нет доступной камеры", Toast.LENGTH_LONG);
                        attached.finish();
                        throw new NoSuchElementException("No available cams");
                    }
                } else if (name.equals("webcam")) {
                    reader.beginArray();
                    reader.beginObject();
                } else if (name.equals("title")) {
                    to.where = reader.nextString();
                } else if (name.equals("preview_url")) {
                    to.picURL = reader.nextString();
                } else {
                    reader.skipValue();
                }
            }
            currentState = Status.JSONPARSED;
            Log.i(TAG, to.where);
            Log.i(TAG, "Successfully parsed JSON");
            result = 0;
        } catch (Exception e) {
            Log.i(TAG, "Something Happened");
            Log.i(TAG, e.getMessage());
            result = 1;
        } finally {
            if (conn != null) conn.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.i(TAG, e.getMessage());
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    Log.i(TAG, e.getMessage());
                }
            }

        }
        return result;
    }

    private int getImageByURL(String URL) {
        int result = -1;
        InputStream bis = null;
        HttpURLConnection conn = null;
        URL url = null;
        try {
            url = new URL(URL);
            conn = (HttpURLConnection) url.openConnection();
            int resp = conn.getResponseCode();
            if (resp != 200) {
                Log.i(TAG, "HTTP Response is not OK");
                Toasty("Сервер веб-камер недоступен", Toast.LENGTH_LONG);
                attached.finish();
                result = 1;
                throw new ConnectException("Connection lost");
            }
            bis = conn.getInputStream();
            inf.pic = BitmapFactory.decodeStream(bis);
            result = 0;
        } catch (Exception e) {
            result = 1;
        } finally {
            if (conn != null) conn.disconnect();
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    Log.i(TAG, e.getMessage());
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        attached.camImageView.setImageBitmap(bitmap);
        attached.progressView.setVisibility(View.GONE);

    }

    enum Status {STARTED, JSONPARSED, ALLCLEAR, FAIL}
}
