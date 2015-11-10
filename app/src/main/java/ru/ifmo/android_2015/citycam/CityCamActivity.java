package ru.ifmo.android_2015.citycam;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.BitmapFactory;
import android.widget.TextView;

/**
 * Экран, показывающий веб-камеру одного выбранного города.
 * Выбранный город передается в extra параметрах.
 */
public class CityCamActivity extends AppCompatActivity {

    /**
     * Обязательный extra параметр - объект City, камеру которого надо показать.
     */
    public static final String EXTRA_CITY = "city";
    public static final String ERROR_MESSAGE = "error";
    private City city;

    private ImageView camImageView;
    private ProgressBar progressView;
    private TextView errorText;

    private DownloadImage downloadTask;

    private Bitmap bitmap = null;

    private String errorMesage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            bitmap = savedInstanceState.getParcelable("bitmap");
            errorMesage = savedInstanceState.getString(ERROR_MESSAGE);
        }
        Log.d(TAG, Boolean.toString(bitmap == null));
        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        setContentView(R.layout.activity_city_cam);
        camImageView = (ImageView) findViewById(R.id.cam_image);
        progressView = (ProgressBar) findViewById(R.id.progress);
        errorText = (TextView) findViewById(R.id.error_text);


        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            Log.d(TAG, "savedInstance");
            downloadTask = (DownloadImage) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask == null) {
            if (bitmap == null) {
                if (errorMesage.isEmpty()) {
                    downloadTask = new DownloadImage(this);
                    downloadTask.execute(city);
                } else {
                    progressView.setVisibility(View.INVISIBLE);
                    errorText.setText(errorMesage);
                }
            } else {
                Log.d(TAG, "New task");
                progressView.setVisibility(View.INVISIBLE);
                camImageView.setImageBitmap(bitmap);
            }
        } else {
            Log.d(TAG, "task is not null!");
            downloadTask.attachActivity(this);
        }

    }

    public class DownloadImage extends AsyncTask<City, Void, Bitmap> {
        private CityCamActivity activity;
        private Bitmap result = null;

        DownloadImage(CityCamActivity activity) {
            this.activity = activity;
        }

        void updateView(Bitmap bitmap) {
            if (activity != null) {
                activity.progressView.setVisibility(View.INVISIBLE);
                activity.camImageView.setImageBitmap(bitmap);
                activity.errorText.setText(errorMesage);
            }
        }

        @Override
        protected Bitmap doInBackground(City... params) {
            try {
                URL url = Webcams.createNearbyUrl(params[0].latitude, params[0].longitude);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                String link = "";
                InputStream res;
                try {
                    res = connection.getInputStream();
                    link = parserJson(res);
                } catch (Exception e) {
                    throw e;
                } finally {
                    connection.disconnect();
                }
                url = new URL(link);
                Log.d(TAG, url.toString());
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                try {
                    res = connection.getInputStream();
                    result = BitmapFactory.decodeStream(res);
                } catch (Exception e) {
                    throw e;
                } finally {
                    connection.disconnect();
                }
                bitmap = result;
                return result;
            } catch (IOException e) {
                errorMesage = "Не удается скачать фото,\n проверьте подключение к сети";
                Log.e(TAG, "In Out Error");
            } catch (Exception e) {
                Log.e(TAG, "No camera error");
                errorMesage = "Для выбранного города камер нет";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            updateView(result);
        }

        void attachActivity(CityCamActivity activity) {
            this.activity = activity;
            updateView(result);
        }

        public String parserJson(InputStream in) throws IOException {
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.beginObject();
            try {
                return jsonObject(reader);
            } catch (Exception e) {
                throw e;
            } finally {
                reader.close();
            }
        }

        String jsonObject(JsonReader reader) throws IOException {
            String lastOne = "";
            while (reader.hasNext()) {
                String name = reader.nextName();
                Log.d(TAG, name);
                switch (name) {
                    case "webcams": {
                        reader.beginObject();
                        lastOne = jsonObject(reader);
                        if (!lastOne.equals("")) {
                            return lastOne;
                        }
                        reader.endObject();
                        break;
                    }
                    case "webcam": {
                        reader.beginArray();
                        reader.beginObject();
                        lastOne = jsonObject(reader);
                        if (!lastOne.equals("")) {
                            return lastOne;
                        }
                        reader.endObject();
                        reader.endArray();
                        break;
                    }
                    case "timelapse": {
                        reader.beginObject();
                        lastOne = jsonObject(reader);
                        if (!lastOne.equals("")) {
                            return lastOne;
                        }
                        reader.endObject();
                        break;
                    }
                    case "categories": {
                        reader.beginObject();
                        lastOne = jsonObject(reader);
                        if (!lastOne.equals("")) {
                            return lastOne;
                        }
                        reader.endObject();
                        break;
                    }
                    case "daylight_preview_url": {
                        lastOne = reader.nextString();
                        return lastOne;
                    }
                    default: {
                        reader.nextString();
                        break;
                    }
                }
            }
            return lastOne;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        super.onSaveInstanceState(toSave);
        toSave.putParcelable("bitmap", bitmap);
        toSave.putString(ERROR_MESSAGE, errorMesage);
    }

    private static final String TAG = "CityCam";
}
