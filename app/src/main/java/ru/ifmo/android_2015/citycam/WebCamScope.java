package ru.ifmo.android_2015.citycam;


import android.view.View;

import ru.ifmo.android_2015.citycam.model.City;


public class WebCamScope {

    CityCamActivity activity;

    WebCamLoader loader;
    WebCam cam;

    WebCamLoader.State state;

    void updateView() {
        switch (state) {
            case STARTED :
            case LOAD_INFO :
            case LOAD_VIEW :
                activity.progressView.setVisibility(View.VISIBLE);
                break;
            case ERROR :
            case NO_CAMERAS :
                activity.progressView.setVisibility(View.INVISIBLE);
                break;
            case FINISHED :
                activity.progressView.setVisibility(View.INVISIBLE);
                activity.ratingBar.setVisibility(View.VISIBLE);
                activity.camImageView.setVisibility(View.VISIBLE);
                activity.titleView.setVisibility(View.VISIBLE);

                activity.ratingBar.setRating((float) cam.getRate());
                activity.titleView.setText(cam.getTitle());
                activity.camImageView.setImageBitmap(cam.getBitmap());
        }
    }

    void updateState(WebCamLoader.State state) {
        activity.loadStateView.setText(state.getInfo());
        this.state = state;

        updateView();
    }

    public void attachActivity(CityCamActivity activity) {
        this.activity = activity;
        updateView();
    }

    public WebCamScope(CityCamActivity activity, City city) {
        this.activity = activity;
        updateState(WebCamLoader.State.STARTED);
        loader = new WebCamLoader(this);
        loader.execute(city);
    }



}
