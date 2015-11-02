package ru.ifmo.android_2015.citycam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
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

        @Override
        protected Integer doInBackground(Void... ignore) {
            try {
                URL connection = Webcams.createNearbyUrl(activity.city.latitude, activity.city.longitude);

               File downloadFile = FileUtils.createTempExternalFile(appContext, "json");
               DownloadUtils.downloadFile(connection, downloadFile);

                FileReader fr = new FileReader(downloadFile);
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(downloadFile)));
                StringBuilder sb = new StringBuilder();
                String data = br.readLine();
                while (data != null) {
                    sb.append(data);
                    System.out.println(data);
                    data = br.readLine();
                }
                JSONObject camData = new JSONObject(sb.toString());
                JSONArray cameras;
                if (camData == null) {
                    return 0;
                } else {
                    cameras = camData.getJSONObject("webcams").getJSONArray("webcam");
                }
                String preURL = cameras.getJSONObject(0).getString("preview_url");
                text = cameras.getJSONObject(0).getString("title");

                File imageFile = FileUtils.createTempExternalFile(appContext, ".image");
                DownloadUtils.downloadFile(new URL(preURL), imageFile);

                camImage = BitmapFactory.decodeStream(new FileInputStream(imageFile));
                //downloadFile(appContext);
                return 0;
            } catch (Exception e) {
                Log.e(TAG, "Error downloading file: " + e, e);
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
