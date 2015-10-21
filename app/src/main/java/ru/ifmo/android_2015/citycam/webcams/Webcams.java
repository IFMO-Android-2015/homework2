package ru.ifmo.android_2015.citycam.webcams;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Константы для работы с Webcams API
 */
public final class Webcams {

    // Зарегистрируйтесь на http://ru.webcams.travel/developers/
    // и вставьте сюда ваш devid
    private static final String DEV_ID = "05f018653116239d76f41ecd86731d18";

    private static final String BASE_URL = "http://api.webcams.travel/rest";

    private static final String PARAM_DEVID = "devid";
    private static final String PARAM_METHOD = "method";
    private static final String PARAM_LAT = "lat";
    private static final String PARAM_LON = "lng";
    private static final String PARAM_FORMAT = "format";
    private static final String PARAM_PAGE = "page";

    private static final String METHOD_NEARBY = "wct.webcams.list_nearby";

    private static final String FORMAT_JSON = "json";

    /**
     * Возвращает URL для выполнения запроса Webcams API для получения
     * информации о веб-камерах рядом с указанными координатами в формате JSON.
     */
    public static URL createNearbyUrl(double latitude, double longitude, int page)
            throws MalformedURLException {
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_METHOD, METHOD_NEARBY)
                .appendQueryParameter(PARAM_LAT, Double.toString(latitude))
                .appendQueryParameter(PARAM_LON, Double.toString(longitude))
                .appendQueryParameter(PARAM_DEVID, DEV_ID)
                .appendQueryParameter(PARAM_FORMAT, FORMAT_JSON)
                .appendQueryParameter(PARAM_PAGE, Integer.toString(page)) // подправил url запрос,
                .build();                                           // добавил текущую страницу
        return new URL(uri.toString());
    }

    private Webcams() {}
}
