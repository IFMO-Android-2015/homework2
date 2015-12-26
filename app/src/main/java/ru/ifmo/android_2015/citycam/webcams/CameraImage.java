package ru.ifmo.android_2015.citycam.webcams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by annafrolova on 27.12.15.
 */
public class CameraImage {
    public enum State{
        NOT_LOAD,
        LOADING,
        LOADED,
        ERROR
    }

    private URL url;
    private State state=State.NOT_LOAD;
    private Bitmap bitmap;

    public CameraImage(URL url)
    {
        this.url=url;
    }
    public  Bitmap getBitmap()
    {
        return  bitmap;
    }
    public State getState()
    {
        return state;
    }
    public void loadImage()
    {
        try {
            state=State.LOADING;
            bitmap=loadBitmap(url);
            state=State.LOADED;
        }
        catch (IOException e)
        {
            state=State.ERROR;
            Log.w(TAG,"Error loading from "+url,e);
        }
    }
    private Bitmap loadBitmap(URL url) throws IOException
    {
        HttpURLConnection connection=(HttpURLConnection)url.openConnection();
        InputStream in=null;
        if(connection.getResponseCode()!=HttpURLConnection.HTTP_OK)
        {
            throw  new IOException("Server returnde code: "+connection.getResponseCode());
        }
        try {
            in=connection.getInputStream();
            Bitmap bitmap= BitmapFactory.decodeStream(in);
            if(bitmap!=null)
            {
                return bitmap;
            }
            else
            {
                throw new IOException("failed in decode bitmap");
            }
        }
        finally {
            if(in!=null)
            {
                in.close();
            }
            connection.disconnect();
        }
    }

    private static final String TAG = "CameraImage";
}
