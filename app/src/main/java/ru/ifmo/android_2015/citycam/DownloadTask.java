package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;

import java.io.IOException;
import java.net.MalformedURLException;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Created by ilnarkadyrov on 12/21/15.
 */
public class DownloadTask extends AsyncTask<City, Void, Boolean>{

    CityCamActivity cityCamActivity;
    Bitmap bitmap;
    enum DownloadProgress{
        IN_PROCESS,
        DOWNLOADED,
        NO_WEBCAMERA
    }
    DownloadProgress downloadProgress = DownloadProgress.IN_PROCESS;

    public DownloadTask(CityCamActivity cityCamActivity){
        this.cityCamActivity = cityCamActivity;
    }

    public void setCityCamActivity(CityCamActivity cityCamActivity){
        this.cityCamActivity = cityCamActivity;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    @Override
    protected Boolean doInBackground(City... params) {
        City city = params[params.length - 1];
        try{
            Webcamera webcamera =
                    DowlnloadWebcam.downloadWebcamerInfoFromJson(Webcams.createNearbyUrl(city.latitude, city.longitude));
            if (webcamera != null){
                bitmap = DowlnloadWebcam.downloadBitmap(webcamera.url);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (bitmap != null);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (cityCamActivity != null){
            if (success){
                cityCamActivity.camImageView.setImageBitmap(bitmap);
                downloadProgress = DownloadProgress.DOWNLOADED;
            } else {
                cityCamActivity.textView.setVisibility(View.VISIBLE);
                downloadProgress = DownloadProgress.NO_WEBCAMERA;
            }
            cityCamActivity.progressView.setVisibility(View.INVISIBLE);
        }
    }
}
