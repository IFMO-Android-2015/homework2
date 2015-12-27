package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Богдан on 27.12.2015.
 */

public class BitmapReader {
    public static Bitmap getBitmap(final String url) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        InputStream inputStream = null;
        Bitmap ans = null;
        try {
            final int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("BR", "Bad HTTP response");
            }
            inputStream = connection.getInputStream();
            ans = BitmapFactory.decodeStream(inputStream);
        } finally {
            if (inputStream != null) inputStream.close();
            connection.disconnect();
        }
        return ans;
    }
}