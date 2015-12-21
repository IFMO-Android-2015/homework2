package ru.ifmo.android_2015.citycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ilnarkadyrov on 12/21/15.
 */
public final class DowlnloadWebcam {
    public static Bitmap downloadBitmap(URL url) throws IOException{
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        InputStream inputStream;
        Bitmap bitmap;
        try {
            inputStream = httpURLConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } finally {
            httpURLConnection.disconnect();
        }
        return bitmap;
    }

    public static Webcamera downloadWebcamerInfoFromJson(URL url) throws IOException{
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        Webcamera webcamera;
        try {
            InputStream inputStream = httpURLConnection.getInputStream();
            webcamera = JsonParser.parseWebcam(inputStream);
        } finally {
            httpURLConnection.disconnect();
        }
        return webcamera;
    }
}
