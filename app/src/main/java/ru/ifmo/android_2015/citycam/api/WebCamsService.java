package ru.ifmo.android_2015.citycam.api;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;
import ru.ifmo.android_2015.citycam.model.WebCamsResult;

/**
 * @author Andreikapolin
 * @date 12.01.16
 */
public interface WebCamsService {
    String BASE_URL = "http://api.webcams.travel";
    String DEV_ID = "66f494500c1670d3a7a6dcf9b7d88e51";
    @GET("/rest?method=wct.webcams.list_nearby&format=json&devid="+DEV_ID)
    Call<WebCamsResult> webCams(@Query("lat") double lat, @Query("lng") double lng);
}
