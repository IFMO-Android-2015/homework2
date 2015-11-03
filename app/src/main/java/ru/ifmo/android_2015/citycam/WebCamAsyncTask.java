package ru.ifmo.android_2015.citycam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import ru.ifmo.android_2015.citycam.webcams.Webcams;
import ru.ifmo.android_2015.citycam.utils.*;

enum DownloadState {
    DOWNLOADING(R.string.downloading),
    DONE(R.string.done),
    ERROR(R.string.error);

    final int state;

    DownloadState(int state) {
        this.state = state;
    }
}

public class WebCamAsyncTask extends AsyncTask<Void, Integer, DownloadState> implements ProgressCallback{

    // Context приложения (Не Activity!) для доступа к файлам
    private Context appContext;
    // Текущий объект Activity, храним для обновления отображения
    private CityCamActivity activity;

    // Текущее состояние загрузки
    private DownloadState state = DownloadState.DOWNLOADING;
    // Прогресс загрузки от 0 до 100
    private int progress;

    private Bitmap image;

    WebCamAsyncTask(CityCamActivity activity) {
        this.appContext = activity.getApplicationContext();
        this.activity = activity;
    }

    void attachActivity(CityCamActivity activity) {
        this.activity = activity;
        updateView();
    }

    /**
     * Вызываем на UI потоке для обновления отображения прогресса и
     * состояния в текущей активности.
     */
    void updateView() {
        if (activity != null) {
            activity.progressView.setProgress(progress);
        }
        if (image != null) {
            activity.progressView.setVisibility(View.INVISIBLE);
            activity.camImageView.setImageBitmap(image);
        }
        if (state == DownloadState.ERROR) {
            activity.error.setText("download failed");
            activity.progressView.setVisibility(View.INVISIBLE);
            activity.error.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Вызывается в UI потоке из execute() до начала выполнения таска.
     */
    @Override
    protected void onPreExecute() {
        updateView();
    }

    /**
     * Скачивание файла в фоновом потоке. Возвращает результат:
     *      0 -- если файл успешно скачался
     *      1 -- если произошла ошибка
     */
    @Override
    protected DownloadState doInBackground(Void... ignore) {
        try {
            getImage();
            state = DownloadState.DONE;

        } catch (Exception e) {
            Log.e("WebCam", "Error downloading file: " + e, e);
            state = DownloadState.ERROR;
        }
        return state;
    }

    void getImage() throws IOException, JSONException {
        URL url = Webcams.createNearbyUrl(activity.city.latitude, activity.city.longitude);
        File file = FileUtils.createTempExternalFile(appContext, "json");
        DownloadUtils.downloadFile(url, file);
        String imageURL = parse(file);
        File image = FileUtils.createTempExternalFile(appContext, "tmp");
        DownloadUtils.downloadFile(new URL(imageURL), image);
        this.image = BitmapFactory.decodeStream(new FileInputStream(image));
    }

    private String parse(File file) throws IOException, JSONException {
        String imageURL = null;
        InputStream in = new FileInputStream(file);
        JsonReader reader = new JsonReader(new InputStreamReader(in));
        reader.beginObject();
        while (reader.hasNext() && !reader.nextName().equals("webcams")) {
            reader.skipValue();
        }
        reader.beginObject();
        while (reader.hasNext() && !reader.nextName().equals("webcam")) {
            reader.skipValue();
        }
        reader.beginArray();
        if (!reader.hasNext()) {
            throw new JSONException("no webcams in this city");
        }
        reader.beginObject();
        while (reader.hasNext() && !reader.nextName().equals("preview_url")) {
            reader.skipValue();
        }
        if (!reader.hasNext()) {
            throw new IOException("preview_url" + "not found");
        } else {
            imageURL = reader.nextString();
            reader.close();
        }
        return imageURL;
    }

    // Метод ProgressCallback, вызывается в фоновом потоке из downloadFile
    public void onProgressChanged(int progress) {
        publishProgress(progress);
    }

    // Метод AsyncTask, вызывается в UI потоке в результате вызова publishProgress
    @Override
    protected void onProgressUpdate(Integer... values) {
        if (values.length > 0) {
            this.progress = values[values.length - 1];
            updateView();
        }
    }

    @Override
    protected void onPostExecute(DownloadState state) {
        // Проверяем код, который вернул doInBackground и показываем текст в зависимости
        // от результата
        this.state = state;
        if (state == DownloadState.DONE) {
            progress = 100;
        }
        updateView();
    }
}

