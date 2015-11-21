package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.WebCam;
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

    private static City city;
    private ImageView camImageView;
    private ProgressBar progressView;
    private DownloadFileTask downloadTask;
    private TextView camTitle;
    private TextView camURL;
    private TextView camLatitude;
    private TextView camLongitude;
    private TextView camLocation;

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
        camTitle = (TextView) findViewById(R.id.CamTitle);
        camURL = (TextView) findViewById(R.id.CamURL);
        camLatitude = (TextView) findViewById(R.id.CamLatitude);
        camLongitude = (TextView) findViewById(R.id.CamLongitude);
        camLocation = (TextView) findViewById(R.id.CamLocation);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);
        if (savedInstanceState != null) {
            downloadTask = (DownloadFileTask) getLastCustomNonConfigurationInstance();
        }
        if (downloadTask == null) {
            downloadTask = new DownloadFileTask(this);
            downloadTask.execute();
        } else {
            downloadTask.ChangeActivity(this);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        downloadTask = null;
    }

    enum processState {
        Error,
        loading,
        Done
    }

    static class DownloadFileTask extends AsyncTask<Void, Void, processState>
    {
        private WebCam camera = null;
        private Bitmap image = null;
        private CityCamActivity activity;
        private processState state = processState.loading;

        DownloadFileTask(CityCamActivity activity) {
            this.activity = activity;
        }

        void ChangeActivity(CityCamActivity activity) {
            this.activity = activity;
            updateView();
        }

        @Override
        protected processState doInBackground(Void ... ignore) {
            HttpURLConnection  urlConnection = null;
            try {
                URL url = Webcams.createNearbyUrl(city.latitude, city.longitude);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream in = null;
                try {
                    in = urlConnection.getInputStream();
                    List<WebCam> result = JSONParser.readJsonStream(in);
                    if (result != null && result.size() > 0) {
                        camera = result.get(0);
                    } else {
                        Log.e(TAG, "No webcam");
                        state = processState.Error;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error of parsing");
                    state = processState.Error;
                } finally {
                    in.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error of connection");
                state = processState.Error;
            }  finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            if (state == processState.Error) {
                return state;
            }
            try {
                URL downloadURL = new URL(camera.getURL());
                image = getImage(downloadURL);
                state = processState.Done;
            } catch (Exception e) {
                Log.e(TAG, "Exception " + e.getMessage());
                state = processState.Error;
            }
            return state;
        }

        private Bitmap getImage(URL downloadURL) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) downloadURL.openConnection();
            connection.connect();
            InputStream inTmp = connection.getInputStream();
            Bitmap result = BitmapFactory.decodeStream(inTmp);
            inTmp.close();
            connection.disconnect();
            return result;
        }

        @Override
        protected void onPostExecute(processState state) {
            this.state = state;
            if (state == processState.Done) {
                activity.progressView.setVisibility(View.INVISIBLE);
                updateView();
            }
            if (state == processState.Error) {
                activity.progressView.setVisibility(View.INVISIBLE);
                Toast.makeText(activity, "In selected city isn't camera", (Toast.LENGTH_LONG)).show();
                Log.e(TAG, "In selected city isn't camera");
            }
        }

        void updateView() {
            if (state == processState.Done && image != null) {
                activity.progressView.setVisibility(View.INVISIBLE);
                activity.camImageView.setImageBitmap(image);
                activity.camTitle.setText("Camera title is " + camera.getTitle());
                activity.camLocation.setText("Camera location is " + camera.getLocation());
                activity.camURL.setText("Camera url is " + camera.getURL());
                activity.camLatitude.setText("Camera latitude is " + camera.getLatitude().toString());
                activity.camLongitude.setText("Camera longitude is " + camera.getLongitude().toString());
            } else if (state == processState.Error) {
                activity.progressView.setVisibility(View.INVISIBLE);
                Log.e(TAG, "Image is null");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("camTitle", camTitle.getText().toString());
        outState.putString("camLocation", camLocation.getText().toString());
        outState.putString("camURL", camURL.getText().toString());
        outState.putString("camLatitude", camLatitude.getText().toString());
        outState.putString("camLongitude", camLongitude.getText().toString());
        Drawable imageTmp = camImageView.getDrawable();
        if (imageTmp != null) {
            outState.putParcelable("camImage", ((BitmapDrawable) imageTmp).getBitmap());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            camTitle.setText(savedInstanceState.getString("camTitle"));
            camLocation.setText(savedInstanceState.getString("camLocation"));
            camURL.setText(savedInstanceState.getString("camURL"));
            camLatitude.setText(savedInstanceState.getString("camLatitude"));
            camLongitude.setText(savedInstanceState.getString("camLongitude"));
            if (downloadTask.state == processState.Done) {
                Bitmap tmp = savedInstanceState.getParcelable("camImage");
                camImageView.setImageBitmap(tmp);
            }
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadTask;
    }

    private static final String TAG = "CityCam";
}
