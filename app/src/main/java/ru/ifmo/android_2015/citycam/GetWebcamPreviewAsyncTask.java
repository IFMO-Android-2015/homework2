package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.WebCam;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Created by sandwwraith(@gmail.com)
 * ITMO University, 2015.
 */

/**
 * Класс запрашивает у API сайта Webcams.travel информацию о находящихся рядом с городом камерах
 */
public class GetWebcamPreviewAsyncTask extends AsyncTask<City, WebCam, Bitmap> {
    private final String LOGTAG = "JsonRequest";
    /**
     * Activity, к которой прикреплен данный таск
     */
    private CityCamActivity activity;

    public GetWebcamPreviewAsyncTask(CityCamActivity activity) {
        this.activity = activity;
        this.progress = Progress.Downloading;
    }

    void attachActivity(CityCamActivity activity) {
        this.activity = activity;
    }

    public enum Progress {Downloading, Error, FetchedData, FetchedBitmap}

    private Progress progress;

    public Progress getProgress() {
        return this.progress;
    }

    /**
     * Показывает загруженное изображение
     *
     * @param bitmap Изображение с вебкамеры.
     */
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        ImageView view = (ImageView) activity.findViewById(R.id.cam_image);
        if (bitmap == null) {
            view.setImageResource(R.drawable.frog);
            this.progress = Progress.Error;
        } else {
            view.setImageBitmap(bitmap);
            this.progress = Progress.FetchedBitmap;
        }

        activity.findViewById(R.id.fetchBar).setVisibility(View.GONE);
    }

    /**
     * @param params Город, камеры рядом с которым необходимо получить (берётся только первый из списка)
     * @return Изображение с камеры, null если произошла ошибка/камеры нет.
     */
    @Override
    protected Bitmap doInBackground(City... params) {
        WebCam cam = fetchCam(params[0]);
        publishProgress(cam);
        return fetchBitMap(cam != null && cam.exists() ? cam.getUrl() : null);
    }

    /**
     * Обновляет UI после получения данных о камере (но до загрузки картинки)
     *
     * @param values Объект WebCam, данные которого выводятся
     */
    @Override
    protected void onProgressUpdate(WebCam... values) {
        WebCam cam = values[0];
        TextView label = (TextView) activity.findViewById(R.id.titleTextView);
        this.progress = Progress.Error;
        if (cam == null) {
            label.setText(R.string.network_error);
        } else if (!cam.exists()) {
            label.setText(R.string.no_cameras);
        } else {
            label.setText(cam.getTitle());
            RatingBar bar = (RatingBar) activity.findViewById(R.id.ratingBar);
            bar.setRating((float) cam.getRating());
            bar.setVisibility(View.VISIBLE);

            TextView upd = (TextView) activity.findViewById(R.id.lastUpdateView);
            DateFormat f = new SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault());
            upd.append("\n" + f.format(cam.getLastUpd()));
            upd.setVisibility(View.VISIBLE);
            this.progress = Progress.FetchedData;
        }
    }

    private WebCam fetchCam(City city) {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            URL request = Webcams.createNearbyUrl(city.latitude, city.longitude);
            conn = (HttpURLConnection) request.openConnection();
            in = conn.getInputStream();
            if (conn.getResponseCode() != 200) {
                throw new IOException("Not-OK response from server: " + conn.getResponseMessage());
            }

            JsonReader reader = new JsonReader(new InputStreamReader(in));
            reader.beginObject();
            while (!reader.nextName().equals("webcams")) {
                reader.skipValue();
            } //Moving to webcam array
            reader.beginObject();
            while (!reader.nextName().equals("webcam")) {
                reader.skipValue();
            }
            //Take only first webcam
            reader.beginArray();
            if (!reader.hasNext()) return new WebCam(); //No cameras in location
            reader.beginObject();
            String preview = null, title = null;
            double rating = 0;
            long lastUpd = 0L;
            while (reader.hasNext()) {
                String name = reader.nextName();

                switch (name) {
                    case "preview_url": {
                        preview = reader.nextString();
                    }
                    break;
                    case "title": {
                        title = reader.nextString();
                    }
                    break;
                    case "rating_avg": {
                        rating = reader.nextDouble();
                    }
                    break;
                    case "last_update": {
                        lastUpd = reader.nextLong();
                    }
                    break;
                    default:
                        reader.skipValue();
                }
            }
            reader.close();
            return new WebCam(preview == null ? null : new URL(preview), title, new Date(lastUpd * 1000 /*Because in milliseconds*/), rating);

        } catch (IOException | IllegalStateException /*Occurs when JSONReader fails */ e) {
            Log.e(LOGTAG, e.getMessage());
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(LOGTAG, e.getMessage());
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private Bitmap fetchBitMap(URL url) {
        if (url == null) return null;
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() != 200) {
                throw new IOException("Not-OK response from server: " + conn.getResponseMessage());
            }
            in = conn.getInputStream();
            return BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            Log.e(LOGTAG, e.getMessage());
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(LOGTAG, e.getMessage());
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
