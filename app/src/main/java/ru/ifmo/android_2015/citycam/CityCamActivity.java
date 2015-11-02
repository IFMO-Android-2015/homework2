package ru.ifmo.android_2015.citycam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ru.ifmo.android_2015.citycam.model.City;

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

    public ImageView camImageView;
    public ProgressBar progressView;
    private GetWebcamData asyncPointer;
    private TextView titleView;
    private String title;

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return asyncPointer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = "";
        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
        }

        setContentView(R.layout.activity_city_cam);
        camImageView = (ImageView) findViewById(R.id.cam_image);
        progressView = (ProgressBar) findViewById(R.id.progress);
        titleView = (TextView) findViewById(R.id.titleView);
        getSupportActionBar().setTitle(city.name);

        progressView.setVisibility(View.VISIBLE);
        ConnectivityManager mgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = mgr.getActiveNetworkInfo();
        if (savedInstanceState == null) {
            if (networkInfo != null && networkInfo.isConnected()) {
                asyncPointer = new GetWebcamData(this);
                asyncPointer.execute(city);

            } else {
                Toast.makeText(getApplicationContext(), "No connection avalaible", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            //CONTINUE ASYNC
            asyncPointer = (GetWebcamData) getLastCustomNonConfigurationInstance();
            asyncPointer.newParent(this);
            if(asyncPointer.currentState== GetWebcamData.Status.FAIL){
                finish();
            }
        }
        // Здесь должен быть код, инициирующий асинхронную загрузку изображения с веб-камеры
        // в выбранном городе.
    }

    public void setTitle(String _title) {
        title = _title;
        titleView.setText(title);
        titleView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if(asyncPointer.currentState== GetWebcamData.Status.FAIL){
            finish();
        }
        String tmp = savedInstanceState.getString("Title");
        if (tmp != "" && (asyncPointer.currentState == GetWebcamData.Status.JSONPARSED || asyncPointer.currentState == GetWebcamData.Status.ALLCLEAR)) {
            setTitle(tmp);
        }
        if (asyncPointer.currentState == GetWebcamData.Status.ALLCLEAR) {
            progressView.setVisibility(View.GONE);
            camImageView.setImageBitmap((Bitmap) savedInstanceState.getParcelable("Image"));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("Title", title);
        if (asyncPointer.currentState == GetWebcamData.Status.ALLCLEAR) {
            outState.putParcelable("Image", ((BitmapDrawable) camImageView.getDrawable()).getBitmap());
        }
    }

    private static final String TAG = "CityCam";
}
