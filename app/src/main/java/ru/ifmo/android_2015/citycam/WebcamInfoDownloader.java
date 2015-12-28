package ru.ifmo.android_2015.citycam;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

public class WebcamInfoDownloader extends AsyncTask<Void, Void, Bitmap> {
    public enum Progress {DECODING_PREVIEW, PARSING_JSON, ERROR, DONE}
    private CityCamActivity attachedActivity;
    private City cityToLoad;
    private Bitmap webcamPreview;
    Progress progress;


    WebcamInfoDownloader(CityCamActivity activity, City cityToLoad) {
        this.attachedActivity = activity;
        this.cityToLoad = cityToLoad;
        progress = Progress.PARSING_JSON;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        JsonReader jsonReader = null;
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        try {
            URL url = Webcams.createNearbyUrl(cityToLoad.latitude,cityToLoad.longitude);
            connection = (HttpURLConnection)url.openConnection();
            inputStream = connection.getInputStream();
            jsonReader = new JsonReader(new InputStreamReader(inputStream));
            parseJSON(jsonReader);
            return webcamPreview;
        }
        catch(Exception e) {
            progress = Progress.ERROR;
            return null;
        }
        finally {
            if(jsonReader != null) {
                try {
                    jsonReader.close();
                } catch(Exception e) {
                    progress = Progress.ERROR;
                }
            }
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch(Exception e) {
                    progress = Progress.ERROR;
                }
            }
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    protected void parseJSON(JsonReader jsonReader) throws Exception {
        for (jsonReader.beginObject(); jsonReader.hasNext(); ) {
            if (jsonReader.nextName().equals("webcams")) {
                for (jsonReader.beginObject(); jsonReader.hasNext(); ) {
                    if (jsonReader.nextName().equals("webcam")) {
                        jsonReader.beginArray();
                        URL camPreviewURL = null;
                        for (jsonReader.beginObject(); jsonReader.hasNext(); ) {
                            switch (jsonReader.nextName()) {
                                case "preview_url":
                                    camPreviewURL = new URL(jsonReader.nextString());
                                    break;
                                default:
                                    jsonReader.skipValue();
                            }
                            if (camPreviewURL != null) {
                                break;
                            }
                        }
                        if (camPreviewURL == null) {
                            break;
                        }
                        progress = Progress.DECODING_PREVIEW;
                        webcamPreview = decodePreview(camPreviewURL);
                        if(webcamPreview == null) {
                            progress = Progress.ERROR;
                        }
                        break;
                    }
                    jsonReader.skipValue();
                }
                break;
            }
            jsonReader.skipValue();
        }
    }

    private Bitmap decodePreview(URL url) {
        if (url == null) return null;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            inputStream = connection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            progress = Progress.ERROR;
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    progress = Progress.ERROR;
                }
            }
            if (connection != null)
                connection.disconnect();
        }
    }

    public Progress getProgress() {
        return progress;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        ImageView view = (ImageView) attachedActivity.findViewById(R.id.cam_image);
        if (progress == Progress.ERROR) {
            view.setImageResource(R.drawable.nocam);
        } else {
            view.setImageBitmap(bitmap);
            progress = Progress.DONE;
        }

        attachedActivity.findViewById(R.id.progress).setVisibility(View.GONE);
    }
}
