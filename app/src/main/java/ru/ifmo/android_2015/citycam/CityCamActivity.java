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

import java.util.Objects;

import ru.ifmo.android_2015.citycam.model.City;
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
    private boolean error;

    private Download_img download_img;
    private RatingBar ratingBar;
    private TextView lable;


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
        lable = (TextView) findViewById(R.id.titleLabel);


        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);

        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
        if(savedInstanceState!=null)
        {
            SaveContainer saveContainer=(SaveContainer)getLastCustomNonConfigurationInstance();
            download_img=saveContainer.download_img;
            error=saveContainer.error;
            cameraDescription=saveContainer.description;
            download_img.setActivity(this);
            updateView();
        }
        else
        {
            download_img=new Download_img(this);
            download_img.execute(city);
        }
    }

    @Override
    public void onDestroy()
    {
        download_img.killActivity();
        super.onDestroy();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance()
    {
        SaveContainer saveContainer=new SaveContainer();
        saveContainer.download_img=download_img;
        saveContainer.error=error;
        saveContainer.description=cameraDescription;
        return saveContainer;
    }

    private void noDataMessage(String massage)
    {
        ratingBar.setVisibility(View.INVISIBLE);
        progressView.setVisibility(View.INVISIBLE);
        lable.setText(massage);
    }

    public void updateError()
    {
        error=true;
        updateView();
    }

    public void updateCamera(CameraDescription description)
    {
        cameraDescription=description;
        updateView();
    }

    private void updateView()
    {
        if(!error)
        {
            showCamera(cameraDescription);
        }
        else
        {
            noDataMessage("error");
        }
    }

    private void showCamera(CameraDescription description)
    {
        if(description!=null)
        {
            ratingBar.setRating((float) description.getRating());
            lable.setText(description.getName());
            CameraImage image=description.getPreviewImage();
            if(image!=null)
            {
                switch (image.getState())
                {
                    case LOADED:
                        showImAndHideProgress(image.getBitmap());
                        break;
                    case ERROR:
                        showImAndHideProgress(null);
                        break;
                }
            }
            else
            {
                noDataMessage("error");
            }
        }
    }

    private void showImAndHideProgress(Bitmap bitmap)
    {
        camImageView.setImageBitmap(bitmap);
        progressView.setVisibility(View.INVISIBLE);
    }

    private static class SaveContainer
    {
        private Download_img download_img;
        private CameraDescription description;
        private boolean error;
    }

    private static final String TAG = "CityCam";
}

