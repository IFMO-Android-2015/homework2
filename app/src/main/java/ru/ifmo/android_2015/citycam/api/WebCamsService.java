package ru.ifmo.android_2015.citycam.api;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import ru.ifmo.android_2015.citycam.model.WebCamsResult;

/**
 * @author creed
 * @date 06.11.15
 */
public interface WebCamsService {
    String BASE_URL = "http://api.webcams.travel";
    String DEV_ID = "15e9f0eb0dd40f6c71d934f3483cd867";
    @GET("/rest?method=wct.webcams.list_nearby&format=json&devid="+DEV_ID)
    Call<WebCamsResult> webCams(@Query("lat") double lat, @Query("lng") double lng);
}
