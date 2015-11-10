package ru.ifmo.android_2015.citycam;

import android.os.AsyncTask;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcams;

public class DownloadAsyncTask extends AsyncTask<City, Void, Webcams> {
    protected Webcams doInBackground(City... params) {
        return ;
    }

}
