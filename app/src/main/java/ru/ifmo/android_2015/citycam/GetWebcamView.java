package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

    private List<Webcam> readArray(JsonReader reader) throws IOException {
        String title = null;
        Long lastUpdate = null;
        String previewURL = null;
        ArrayList<Webcam> result = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "title":
                        title = reader.nextString();
                        break;
                    case "last_update":
                        lastUpdate = reader.nextLong();
                        break;
                    case "preview_url":
                        previewURL = reader.nextString();
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
            result.add(new Webcam(title, lastUpdate, previewURL));
        }
        reader.endArray();
        return result;
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
            JsonReader reader = new JsonReader(new InputStreamReader(input, "UTF-8"));
            ArrayList<Webcam> webcams = new ArrayList<>();

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "status":
                        String message = reader.nextString();
                        if (message.equals("fail")) {
                            throw new Exception("Request Failed");
                        }
                        break;
                    case "webcams":
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String inner_name = reader.nextName();
                            if (inner_name.equals("webcam")) {
                                webcams = (ArrayList<Webcam>)readArray(reader);
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                        break;
                    default:
                        reader.skipValue();

                }
            }
            reader.endObject();

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
