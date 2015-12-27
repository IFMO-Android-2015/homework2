package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.MyCamera;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

import static ru.ifmo.android_2015.citycam.webcams.Webcams.createNearbyUrl;


public class DownloadInformationTask extends AsyncTask<City, MyCamera, MyCamera> {

    City thisCity;
    private static final String TAG = "myLogs";
    public enum ProbableExeptions {
        NO_EXEPTIONS, NO_CAMERAS, BAD_CONNECTION, NO_PREVIEW_URL;
    }
    public CityCamActivity cityCamActivity;
    ProbableExeptions catched;

    public DownloadInformationTask (CityCamActivity cityCamActivity) {
        catched = ProbableExeptions.NO_EXEPTIONS;
        this.cityCamActivity = cityCamActivity;
    }



    protected MyCamera doInBackground(City... cities) {
        thisCity = cities[0];
        try {
            MyCamera myCamera = tryToDownloadCity(cities[0]);
            if (myCamera != null) {

                URL url = new URL(myCamera.preview_url);
                Bitmap bitmap = tryToGetImage(url);
                myCamera.setCameraImage(bitmap);
                myCamera.setCityName(cities[0].name);

                return myCamera;
            }
            Log.w(TAG, "MyCamera == null");
        }
        catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(MyCamera myCamera) {
        if (catched != ProbableExeptions.NO_EXEPTIONS) {
            switch (catched) {
                case NO_CAMERAS:
                    setExeptionView("У города " + thisCity.name + " нет вебкамер");
                    break;
                case BAD_CONNECTION:
                    setExeptionView("Плохой соединение, попробуйте повторить загрузку");
                    break;
                case NO_PREVIEW_URL:
                    setExeptionView("Не удалось получить изображение, попробуйте повторить загрузку");
                    break;
            }
            return;
        }

        ImageView image = (ImageView) cityCamActivity.findViewById(R.id.cam_image);
        ProgressBar progressBar = (ProgressBar) cityCamActivity.findViewById(R.id.progress);
        String camInfo = "";
        TextView textCamInfo = (TextView) cityCamActivity.findViewById(R.id.camInfo);

        if (myCamera.cameraImage == null) {
            setExeptionView("Не удалось получить изображение, попробуйте повторить загрузку");
            return;
        }
        progressBar.setVisibility(ProgressBar.GONE);
        image.setImageBitmap(myCamera.cameraImage);

        if (myCamera.cityName != null)
            camInfo += "Название города: " + myCamera.cityName + "\n";
        if (myCamera.user != null)
            camInfo += "Пользователь: " + myCamera.user + "\n";
        if (myCamera.user_url != null)
            camInfo += "(ссылка на пользователя: " + myCamera.user_url + "\n";
        if (myCamera.title != null)
            camInfo += "Тайтл: " + myCamera.title + "\n";
        textCamInfo.setText(camInfo);

    }

    private void setExeptionView(String s) {
        ProgressBar progressBar = (ProgressBar) cityCamActivity.findViewById(R.id.progress);
        progressBar.setVisibility(ProgressBar.GONE);
        ImageView image = (ImageView) cityCamActivity.findViewById(R.id.cam_image);
        image.setVisibility(ImageView.INVISIBLE);
        Toast.makeText(cityCamActivity, s, Toast.LENGTH_LONG).show();

    }

    private Bitmap tryToGetImage(URL url) throws IOException {
        if (url == null) {
            catched = ProbableExeptions.NO_PREVIEW_URL;
            return null;
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream in = connection.getInputStream();
        Bitmap bitmap;

        try {
            bitmap = BitmapFactory.decodeStream(in);
            return bitmap;
        } finally {
            if (in != null)
                in.close();
            connection.disconnect();
        }

    }

    private MyCamera tryToDownloadCity(City city) throws IOException {
        URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream in = null;

        try {
            in = connection.getInputStream();
        } catch (Exception e) {
            catched = ProbableExeptions.BAD_CONNECTION;
        }

        try {
            JsonReader reader = new JsonReader(new InputStreamReader(in));
            MyCamera myCamera = tryToRead(reader);
            reader.close();
            return myCamera;
        } finally {
            if (in != null)
                in.close();
            connection.disconnect();
        }
    }

    private MyCamera tryToRead(JsonReader reader) throws IOException{
        String user = null;
        String user_url = null;
        String title = null;
        String preview_url = null;

        reader.beginObject();

        while (reader.hasNext()) {
            String infoParse = reader.nextName();

            if (infoParse.equals("webcams")) {

                reader.beginObject();

                while (reader.hasNext()) {
                    String webcamsInfo = reader.nextName();

                    if (webcamsInfo.equals("count")) {
                        int count = reader.nextInt();
                        if (count == 0) {
                            catched = ProbableExeptions.NO_CAMERAS;
                            return null;
                        }
                    } else
                    if (webcamsInfo.equals("webcam")) {

                        reader.beginArray();

                        while (reader.hasNext()) {
                            reader.beginObject();
                            while (reader.hasNext()) {
                                String webcamInfo = reader.nextName();

                                switch (webcamInfo) {
                                    case "user":
                                        user = reader.nextString();
                                        break;
                                    case "user_url":
                                        user_url = reader.nextString();
                                        break;
                                    case "title":
                                        title = reader.nextString();
                                        break;
                                    case "preview_url":
                                        preview_url = reader.nextString();
                                        break;
                                    default:
                                        reader.skipValue();
                                }
                            }
                            reader.endObject();
                        }
                        reader.endArray();
                        return new MyCamera(preview_url, user, user_url, title);
                    }
                    else
                        reader.skipValue();
                }
                reader.endObject();
            }
            else
                reader.skipValue();
        }
        reader.endObject();

        catched = ProbableExeptions.BAD_CONNECTION;
        return null;
    }

}
