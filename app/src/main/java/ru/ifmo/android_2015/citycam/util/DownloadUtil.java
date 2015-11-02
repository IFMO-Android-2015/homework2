package ru.ifmo.android_2015.citycam.util;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by artem on 30.10.15.
 */
public class DownloadUtil {
    public static void downloadFile(URL url, File destination) {

        HttpURLConnection connection = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            Log.d("connection - ", "Received HTTP response code: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new FileNotFoundException("Unexpected HTTP response: " + responseCode
                        + ", " + connection.getResponseMessage());
            }

            int contentLength = connection.getContentLength();
            Log.d("Content Length: ", String.valueOf(contentLength));

            byte[] buffer = new byte[1024 * 128];
            int receivedBytes;
            int receivedLength = 0;
            int progress = 0;

            in = connection.getInputStream();
            out = new FileOutputStream(destination);

            while ((receivedBytes = in.read(buffer)) >= 0) {
                out.write(buffer, 0, receivedBytes);
                receivedLength += receivedBytes;

//                if (contentLength > 0) {
//                    int newProgress = 100 * receivedLength / contentLength;
//                    if (newProgress > progress && progressCallback != null) {
//                        Log.d(TAG, "Downloaded " + newProgress + "% of " + contentLength + " bytes");
//                        progressCallback.onProgressChanged(newProgress);
//                    }
//                    progress = newProgress;
//                }

            }
            if (receivedLength != contentLength) {
                Log.w("Received ", receivedLength + " bytes, but expected " + contentLength);
            } else {
                Log.d("Received ",  receivedLength + " bytes");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e("Failed to close HTTP input stream",  e.toString(), e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e("Failed to close file", e.toString(), e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
