package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Random;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.Webcam;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

/**
 * Экран, показывающий веб-камеру одного выбранного города.
 * Выбранный город передается в extra параметрах.
 */
public class CityCamActivity extends AppCompatActivity {

    /**
     * Обязательный extra параметр - объект City, камеру которого надо показать.
     */
    public static final String EXTRA_CITY = "city";
    private static final String TAG = "CityCam";
    private City city;
    private ImageView camImageView;
    private ProgressBar progressView;
    private DownloadTask downloadTask;
    private TextView webcamCityText;
    private TextView webcamRatingText;
    private TextView webcamTitleText;
    private LinearLayout linear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        setContentView(R.layout.activity_city_cam);
        camImageView = (ImageView) findViewById(R.id.cam_image);
        progressView = (ProgressBar) findViewById(R.id.progress);
        webcamTitleText = (TextView) findViewById(R.id.webcam_title);
        webcamCityText = (TextView) findViewById(R.id.webcam_city);
        webcamRatingText = (TextView) findViewById(R.id.webcam_rating);
        linear = (LinearLayout) findViewById(R.id.linear_id);
        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            downloadTask = (DownloadTask) getLastCustomNonConfigurationInstance();
        }

        if (savedInstanceState == null) {
            downloadTask = new DownloadTask(this);
            downloadTask.execute();
        } else {
            downloadTask.attachActivity(this);
        }
        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
    }

    public Object onRetainCustomNonConfigurationInstance() {
        return downloadTask;
    }


    private enum Result {
        INPROGRESS, OK, NOCAM, ERROR
    }

    private class DownloadTask extends AsyncTask<Void, Void, Result> {

        private CityCamActivity activity = null;
        private Bitmap picture = null;
        private Webcam webcam = null;
        private Result result = Result.INPROGRESS;

        public DownloadTask(CityCamActivity activity) {
            this.activity = activity;
        }

        public void attachActivity(CityCamActivity activity) {
            this.activity = activity;
            publishProgress();
        }

        @Override
        protected Result doInBackground(Void... params) {
            Log.i(TAG, "Task started");
            try {
                HttpURLConnection conn = (HttpURLConnection)
                        Webcams.createNearbyUrl(city.latitude, city.longitude)
                                .openConnection();
                Log.i(TAG, "Connection opened");
                InputStream in = new BufferedInputStream(conn.getInputStream());
                Log.i(TAG, "Stream opened");
                WebcamsParser webcamsParser = new WebcamsParser();
                List<Webcam> list = webcamsParser.readStream(in);
                Log.i(TAG, "Webcams parsed " + list.size());
                if (list == null) {
                    result = Result.ERROR;
                    return result;
                } else if (list.size() == 0) {
                    result = Result.NOCAM;
                    return result;
                }
                Random rand = new Random();
                webcam = list.get(rand.nextInt(list.size()));
                picture = webcamsParser.downloadBitmapImage(webcam.imageUrl);

                conn.disconnect();
            } catch (Exception e) {
                return Result.ERROR;
            }
            result = Result.OK;
            return result;
        }

        @Override
        protected void onPostExecute(Result res) {
            result = res;
            switch (result) {
                case ERROR:
                    picture = textAsBitmap("FAILED", 90, Color.RED);
                    Toast.makeText(getApplicationContext(), "Failed to load", Toast.LENGTH_SHORT).show();
                    break;
                case NOCAM:
                    picture = textAsBitmap("NO CAMS", 90, Color.RED);
                    Toast.makeText(getApplicationContext(), "There are no webcams in this city", Toast.LENGTH_LONG).show();
                    break;
            }
            updateUI();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            updateUI();
        }

        private void updateUI() {
            if (result != Result.INPROGRESS && picture != null) {
                activity.progressView.setVisibility(View.GONE);
                activity.camImageView.setImageBitmap(picture);
                if (result == Result.OK) {
                    activity.linear.setVisibility(View.VISIBLE);
                    activity.webcamTitleText.setText(activity.webcamTitleText.getText() + " " + webcam.title);
                    activity.webcamCityText.setText(activity.webcamCityText.getText() + " " + webcam.city);
                    activity.webcamRatingText.setText(activity.webcamRatingText.getText() + " " + webcam.rating);
                }
            }
        }

        public Bitmap textAsBitmap(String text, float textSize, int textColor) {
            Paint paint = new Paint();
            paint.setTextSize(textSize);
            paint.setColor(textColor);
            paint.setTextAlign(Paint.Align.CENTER);
            Bitmap image = Bitmap.createBitmap(camImageView.getWidth(), camImageView.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(image);
            int xPos = (canvas.getWidth() / 2);
            int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
            canvas.drawText(text, xPos, yPos, paint);
            return image;
        }
    }
}
