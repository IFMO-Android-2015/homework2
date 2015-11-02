package ru.ifmo.android_2015.citycam;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

public class WebCamLoader extends AsyncTask<City, WebCamLoader.State, WebCam> {

    public final static String LOG = "WebCamLoader";

    enum State {
        STARTED("Начало загрузки"),
        LOAD_INFO("Загрузка инофрмации о камере..."),
        LOAD_VIEW("Загрузка изображения с камеры..."),
        NO_CAMERAS("Нет камер по-близости."),
        ERROR("Ошибка загрузки."),
        FINISHED("Загрузка завершена.");

        private String info;

        public String getInfo() {
            return info;
        }

        State(String info) {
            this.info = info;
        }
    }

    private WebCamScope scope;

    public WebCamLoader(WebCamScope scope) {
        this.scope = scope;
    }

    @Override
    protected void onProgressUpdate(State... states) {
        scope.updateState(states[0]);
    }


    @Override
    protected void onPostExecute(WebCam webCam) {
        scope.cam = webCam;
        if (webCam != null) scope.updateState(State.FINISHED);
    }

    @Override
    protected WebCam doInBackground(City... city) {
        publishProgress(State.LOAD_INFO);

        HttpURLConnection connection = null;
        InputStream in = null;
        JsonReader reader = null;

        String url_str = null, title = null;
        double rate = 0;

        try {
            URL url = Webcams.createNearbyUrl(city[0].latitude, city[0].longitude);
            connection = (HttpURLConnection) url.openConnection();
            in = connection.getInputStream();
            reader = new JsonReader(new InputStreamReader(in));

            reader.beginObject();
            while (!reader.nextName().equals("webcams"))
                reader.skipValue();                             //skip until "webcams"
            reader.beginObject();                               //enter to "webcams"
            while (!reader.nextName().equals("webcam"))
                reader.skipValue();                             //skip until first "webcam"
            reader.beginArray();
            if (!reader.hasNext()) {
                publishProgress(State.NO_CAMERAS);
                return null;
            }
            reader.beginObject();                               //enter to "webcam"

            while(reader.hasNext()) {
                switch(reader.nextName()) {
                    case "preview_url" :
                        url_str = reader.nextString();
                        break;
                    case "title" :
                        title  = reader.nextString();
                        break;
                    case "rating_avg" :
                        rate = reader.nextDouble();
                        break;
                    default:
                        reader.skipValue();
                }
            }

            reader.close();

        } catch (IOException e) {
            Log.d(LOG, "Error while loading WebCamera information", e);
            publishProgress(State.ERROR);
        } finally {
            // closing files if it wasn't
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                Log.d(LOG,"Can't close <InputStream in>", e);
            }
            if (connection != null)
                connection.disconnect();
        }

        if( url_str == null)
            return null;

        Bitmap bitmap = null;

        try {
            publishProgress(State.LOAD_VIEW);
            connection = (HttpURLConnection) new URL(url_str).openConnection();
            in = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            publishProgress(State.ERROR);
            Log.d(LOG, "Error while loading Bitmap camera view", e);
        } finally {
            // closing files if it wasn't
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                Log.d(LOG,"Can't close <InputStream in>", e);
            }
            if (connection != null)
                connection.disconnect();
        }

        if(bitmap == null) {
            return null;
        }
        WebCam cam = new WebCam(bitmap, rate, title);
        return cam;

    }


}
