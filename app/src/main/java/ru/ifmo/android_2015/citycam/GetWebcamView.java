package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Created by ZeRoGerc
 * ITMO UNIVERSITY
 */
public class GetWebcamView extends AsyncTask<City, Webcam, Webcam> {
    private CityCamActivity activity;

    enum DownloadState {DOWNLOADING, DONE, ERROR}

    private DownloadState state = DownloadState.DOWNLOADING;

    GetWebcamView(CityCamActivity activity) {
        this.activity = activity;
    }

    void attachActivity(CityCamActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Webcam doInBackground(City... params) {
        Webcam result = new Webcam();
        City city = params[0];
        HttpURLConnection con = null;
        InputStream input = null;
        try {
            URL webcamsUrl = Webcams.createNearbyUrl(city.latitude, city.longitude);
            con = (HttpURLConnection) webcamsUrl.openConnection();
            input = con.getInputStream();
            JSONObject parsedJSON = new JSONObject(new BufferedReader(new InputStreamReader(input)).readLine());
            if (!parsedJSON.getString("status").equals("ok")) {
                throw new Exception("Request Failed");
            }
            parsedJSON = parsedJSON.getJSONObject("webcams");
            JSONArray webcamsArray = parsedJSON.getJSONArray("webcam");
            ArrayList<Webcam> webcams = new ArrayList<>();
            for (int i = 0; i < webcamsArray.length(); i++) {
                webcams.add(new Webcam(webcamsArray.getJSONObject(i)));
            }
            if (webcams.size() == 0) {
                result.attachImage(null);
            } else {
                result = webcams.get(new Random().nextInt(webcams.size()));
                result.attachImage(getBitMapByWebcam(result));
            }
            return result;
        }
        catch (Exception e) {
            Log.e(this.TAG, e.getMessage());
        }
        finally {
            if (con != null) {
                con.disconnect();
            }
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    Log.e(this.TAG, e.getMessage());
                }
            }
        }
        return null;
    }

    private Bitmap getBitMapByWebcam(Webcam webcam) {
        HttpURLConnection connection = null;
        InputStream input = null;
        try {
            connection = (HttpURLConnection)webcam.getPreviewURL().openConnection();
            input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        }
        catch (Exception e) {
            Log.e(this.TAG, e.getMessage());
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    Log.e(this.TAG, e.getMessage());
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Webcam webcam) {
        if (webcam == null) {
            state = DownloadState.ERROR;
            activity.errorOccurred();
        } else {
            if (webcam.getImage() == null) {
                state = DownloadState.ERROR;
            } else {
                state = DownloadState.DONE;
            }
            activity.printImage(webcam);
        }
    }

    public DownloadState getState() {
        return this.state;
    }

    private final String TAG = "AsyncTask";
}
