package ru.ifmo.android_2015.citycam.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ruslandavletshin on 02/11/15.
 */
public class BitmapLoader {
    public static Bitmap getBitmap (final String url) throws IOException{
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        InputStream in = null;
        Bitmap r = null;

        try {
            final int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new FileNotFoundException("Bad HTTP response: " + responseCode);
            }


            in = connection.getInputStream();
            r = BitmapFactory.decodeStream(in);

        } finally {
            if (in != null) in.close();
            connection.disconnect();
        }
        return r;
    }
}
