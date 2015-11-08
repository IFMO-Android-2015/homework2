package ru.ifmo.android_2015.citycam.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.citycam.webcams.Webcam;
/**
 * Методы для скачивания файлов.
 */
public final class DownloadUtils {

    public static Bitmap downloadBitmap(URL downloadUrl) throws IOException{
        HttpURLConnection conn = (HttpURLConnection) downloadUrl.openConnection();
        Log.i(TAG, "Opened connection");

        InputStream in = null;
        Bitmap bitmap = null;

        try {
            in = conn.getInputStream();
            Log.i(TAG, "Got inputStream");

            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e){
            Log.e(TAG, e.getMessage());
        } finally {
            conn.disconnect();
        }
        return bitmap;
    }

    public static Webcam downloadWebcamInfo(URL downloadUrl) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) downloadUrl.openConnection();
        Log.i(TAG, "Opened connection");
        Webcam webcam = null;

        try {
            InputStream in = conn.getInputStream();
            Log.i(TAG, "Got inputStream");

            webcam = JSONUtil.getWebcam(in);
            Log.i(TAG, "Got webcam info");
        } catch (IOException e){
            Log.e(TAG, e.getMessage());
        } finally {
            conn.disconnect();
        }
        return webcam;
    }

    private static final String TAG = "Download";

    private DownloadUtils() {}
}
