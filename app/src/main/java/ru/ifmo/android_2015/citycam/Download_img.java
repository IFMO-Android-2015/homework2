package ru.ifmo.android_2015.citycam;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.CameraApi;
import ru.ifmo.android_2015.citycam.webcams.CameraDescription;

/**
 * Created by annafrolova on 27.12.15.
 */
public class Download_img extends AsyncTask<City,CameraDescription,CameraDescription> {
    private CityCamActivity activity;
    private boolean flage;

    public Download_img(CityCamActivity activity)
    {
        this.activity=activity;
    }
    public void setActivity(CityCamActivity activity)
    {
        this.activity=activity;
    }
    public void killActivity()
    {
        this.activity=null;
    }

    @Override
    protected CameraDescription doInBackground(City... params)
    {
        City city=params[0];
        CameraDescription cameraDescription=null;
        try {
            cameraDescription= CameraApi.getCameraDescription(city.latitude,city.longitude);
            if(cameraDescription==null)
            {
                return null;
            }
            publishProgress(cameraDescription);
            cameraDescription.getPreviewImage().loadImage();
        }
        catch (IOException e)
        {
            flage=true;
            Log.w(TAG,"Error in load camera data for "+params[0], e);
            return null;
        }
        cameraDescription.getPreviewImage().loadImage();
        return cameraDescription;
    }

    @Override
    protected void onProgressUpdate(CameraDescription... descriptions)
    {
        CameraDescription description=descriptions[0];
        activity.updateCamera(description);
    }

    @Override
    protected void onPostExecute(CameraDescription description)
    {
        if(!flage)
        {
            activity.updateCamera(description);
        }
        else
        {
            activity.updateError();
        }
    }

    private static final String TAG="Download_img";
}
