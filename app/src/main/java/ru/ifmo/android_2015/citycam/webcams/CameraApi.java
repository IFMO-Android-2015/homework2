package ru.ifmo.android_2015.citycam.webcams;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by annafrolova on 27.12.15.
 */
public class CameraApi {
    public static CameraDescription getCameraDescription(double x,double y) throws IOException
    {
        try {
            URL nearbyCameraListUrl=Webcams.createNearbyUrl(x,y);
            HttpURLConnection connection=(HttpURLConnection)nearbyCameraListUrl.openConnection();
            InputStream in=null;
            try {
                in=connection.getInputStream();
                JsonReader jsonReader=new JsonReader(new InputStreamReader(in));
                jsonReader.beginObject();
                while (jsonReader.hasNext())
                {
                    String name=jsonReader.nextName();
                    switch (name)
                    {
                        case "status":
                            String status=jsonReader.nextString();
                            if(!status.equals("ok"))
                            {
                                throw new IOException("API error: status not ok");
                            }
                            break;
                        case "webcams":
                            return readFromWebcams(jsonReader);
                        default:
                            jsonReader.skipValue();
                            break;
                    }
                }
                jsonReader.endObject();
            }
            finally {
                if(in!=null)
                {
                    in.close();
                }
                connection.disconnect();
            }
        }
        catch (IOException | IllegalStateException e)
        {
            throw new IOException("API error: ",e);
        }
        throw new IOException("API error: invalide format");
    }
    private static CameraDescription readFromWebcams(JsonReader jsonReader) throws IOException
    {
        jsonReader.beginObject();
        while (jsonReader.hasNext())
        {
            String name=jsonReader.nextName();
            switch (name)
            {
                case "count":
                    if(jsonReader.nextInt()==0)
                    {
                        return null;
                    }
                    break;
                case "webcam":
                    return FirstCameraDescription(jsonReader);
                default:
                    jsonReader.skipValue();
                    break;
            }
        }
        jsonReader.endObject();;
        throw new IOException("API error: no camera found");
    }
    private static CameraDescription FirstCameraDescription(JsonReader jsonReader) throws IOException
    {
        jsonReader.beginArray();
        if(jsonReader.hasNext())
            return readCamera(jsonReader);
        else
            return null;
    }
    private static CameraDescription readCamera(JsonReader jsonReader) throws IOException
    {
        jsonReader.beginObject();
        String url=null,title=null;
        double rating=0;
        while (jsonReader.hasNext())
        {
            String name=jsonReader.nextName();
            switch (name)
            {
                case "title":
                    title=jsonReader.nextString();
                    break;
                case "preview_url":
                    url=jsonReader.nextString();
                    break;
                case "rating_avg":
                    rating=jsonReader.nextDouble();
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }
        jsonReader.endObject();
        if(url!=null)
        {
            return new CameraDescription(title,new CameraImage(new URL(url)),rating);

        }
        else
        {
            return new CameraDescription(title,null,rating);
        }
    }
}
