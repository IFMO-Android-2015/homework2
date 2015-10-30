package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.tasks.LoadCameraAsyncTask;
import ru.ifmo.android_2015.citycam.webcams.CameraDescription;
import ru.ifmo.android_2015.citycam.webcams.CameraImage;

/**
 * Экран, показывающий веб-камеру одного выбранного города.
 * Выбранный город передается в extra параметрах.
 */
public class CityCamActivity extends AppCompatActivity {

    /**
     * Обязательный extra параметр - объект City, камеру которого надо показать.
     */
    public static final String EXTRA_CITY = "city";

    private City city;

    private CameraDescription cameraDescription;
    private boolean errorOccurred;

    private LoadCameraAsyncTask downloadTask;
    private RatingBar ratingBar;
    private TextView titleLabel;

    private ImageView camImageView;
    private ProgressBar progressView;

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
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        titleLabel = (TextView) findViewById(R.id.titleLabel);

        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
        if (savedInstanceState == null) {
            downloadTask = new LoadCameraAsyncTask(this);
            downloadTask.execute(city);
        } else {
            SaveContainer save = (SaveContainer) getLastCustomNonConfigurationInstance();
            downloadTask = save.downloadTask;
            errorOccurred = save.errorOccurred;
            cameraDescription = save.description;
            downloadTask.bindActivity(this);
            updateView();
        }
    }

    @Override
    protected void onDestroy() {
        downloadTask.unbindActivity();
        super.onDestroy();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        SaveContainer saveContainer = new SaveContainer();
        saveContainer.downloadTask = downloadTask;
        saveContainer.errorOccurred = errorOccurred;
        saveContainer.description = cameraDescription;
        return saveContainer;
    }

    private void showNoDataMessage(String message) {
        ratingBar.setVisibility(View.INVISIBLE);
        progressView.setVisibility(View.INVISIBLE);
        titleLabel.setText(message);
    }

    public void updateError() {
        errorOccurred = true;
        updateView();
    }

    public void updateCameraDescription(CameraDescription description) {
        cameraDescription = description;
        updateView();
    }

    private void updateView() {
        if (!errorOccurred) {
           showCameraDescription(cameraDescription);
        } else {
            showNoDataMessage(getString(R.string.city_cam_error));
        }
    }

    private void showCameraDescription(CameraDescription description) {
        if (description != null) {
            ratingBar.setRating((float) description.getAverageRating());
            titleLabel.setText(description.getTitle());

            CameraImage previewImage = description.getPreviewImage();

            if (previewImage != null) {
                switch (previewImage.getState()) {
                    case LOADED:
                        showImageAndHideProgress(previewImage.getBitmap());
                        break;
                    case ERROR:
                        showImageAndHideProgress(null);
                        break;
                }
            }
        } else {
            showNoDataMessage(getString(R.string.city_cam_no_camera));
        }
    }

    private void showImageAndHideProgress(Bitmap image) {
        camImageView.setImageBitmap(image);
        progressView.setVisibility(View.INVISIBLE);
    }

    private static class SaveContainer {
        private LoadCameraAsyncTask downloadTask;
        private CameraDescription description;
        private boolean errorOccurred;
    }

    private static final String TAG = "CityCam";
}
