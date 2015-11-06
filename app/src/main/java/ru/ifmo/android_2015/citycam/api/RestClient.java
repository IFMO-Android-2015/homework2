package ru.ifmo.android_2015.citycam.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import ru.ifmo.android_2015.citycam.CityCamActivity;
import ru.ifmo.android_2015.citycam.SelectCityActivity;
import ru.ifmo.android_2015.citycam.model.WebCamsResult;

/**
 * @author creed
 * @date 06.11.15
 */
public class RestClient {
    private static WebCamsService service = null;

    private static WebCamsService getService() {
        if (service == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(WebCamsService.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            service = retrofit.create(WebCamsService.class);
        }
        return service;
    }

    @Nullable
    public static WebCamsResult webCams(double lat, double lng) {
        Response<WebCamsResult> result = null;
        try {
             result = getService().webCams(lat, lng).execute();
        } catch (IOException e) {
            Log.e(SelectCityActivity.TAG, e.getMessage());
        }
        return (result == null ? null : result.body());
    }
}

