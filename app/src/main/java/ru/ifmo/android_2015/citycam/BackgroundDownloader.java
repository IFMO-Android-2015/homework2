package ru.ifmo.android_2015.citycam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.citycam.webcams.Webcams;

class BackgroundDownloader extends AsyncTask<Void, Integer, Integer> {

        private Context appContext;
        private CityCamActivity activity;
        private Bitmap camImage;
        private String text;
        BackgroundDownloader(CityCamActivity activity) {
            this.appContext = activity.getApplicationContext();
            this.activity = activity;
        }

        void attachActivity(CityCamActivity activity) {
            this.activity = activity;
        }

        Object[] getData(File file) throws IOException{
            InputStreamReader reader = null;
            BufferedReader br = null;
            JsonReader jreader = null;
            Object[] temp = null;
            try {
                reader = new FileReader(file);
                br = new BufferedReader(reader);
                jreader = new JsonReader(br);
            } finally {

                if (reader != null) {
                    reader.close();
                }
                if (br != null) {
                    br.close();
                }
                if (jreader != null) {
                    jreader.close();
                }

            }

            return temp;
        }

        String jsonField(File file, String field) throws  IOException{
            String answer = "";
            InputStream is = null;
            FileReader fr = null;
            JsonReader jr = null;
            try {
                is = new FileInputStream(file);
                fr = new FileReader(file);
                jr = new JsonReader(fr);
                jr.beginObject();
                while (jr.hasNext()) {
                    String name ="";
                    switch (jr.nextName()) {
                        case "webcams":
                            jr.beginObject();
                            while (jr.hasNext()) {
                                name = jr.nextName();
                                if (name.equals("webcam")) {
                                    break;
                                } else {
                                    jr.skipValue();
                                }
                            }
                            if (!name.equals("webcam")) {
                                throw new JSONException("unexpected end");
                            }
                            jr.beginArray();
                            if (!jr.hasNext()) {
                                throw  new JSONException("unexpected end");
                            }
                            jr.beginObject();
                            String name2 = "";
                            while (jr.hasNext()) {
                                name2 = jr.nextName();
                                if (name2.equals(field)) {
                                    break;
                                } else {
                                    jr.skipValue();
                                }
                            }
                            if (name2.equals(field)) {
                                answer = jr.nextString();
                            } else {
                                throw new JSONException("not found field in json");
                            }
                            break;
                        default:
                            jr.skipValue();
                    }
                }
            } catch (Exception e) {

                Log.e(TAG, "Error with json parsing: " + e, e);
            } finally {
                if (is != null) {
                    is.close();
                }
                if (jr != null) {
                    jr.close();
                }
                if (fr != null) {
                    fr.close();
                }
            }
            return  answer;
        }
        @Override
        protected Integer doInBackground(Void... ignore) {
            try {
                URL url = Webcams.createNearbyUrl(activity.city.latitude, activity.city.longitude);

                File downloadFile = FileUtils.createTempExternalFile(appContext, "json");
                DownloadUtils.downloadFile(url, downloadFile);
                String preURL = jsonField(downloadFile, "preview_url");
                text = jsonField(downloadFile, "title");

                File imageFile = FileUtils.createTempExternalFile(appContext, ".image");
                DownloadUtils.downloadFile(new URL(preURL), imageFile);

                camImage = BitmapFactory.decodeStream(new FileInputStream(imageFile));

                return 0;
            } catch (Exception e) {
                Log.e(TAG, "Error downloading file: " + e, e);
            } finally {
            }
            return 1;
        }


        @Override
        protected void onPostExecute(Integer state) {
            updateView();
        }

        void updateView() {
            if (this.activity != null) {
                if (camImage != null) {
                    activity.camImageView.setImageBitmap(camImage);
                    activity.progressView.setVisibility(View.INVISIBLE);
                    activity.textView.setText(text);
                    activity.image = camImage;
                    Data.city = activity.city;
                    Data.image = camImage;
                    Data.title = text;
                }
            }
        }
        private static final String TAG = "CityCam";
    }
