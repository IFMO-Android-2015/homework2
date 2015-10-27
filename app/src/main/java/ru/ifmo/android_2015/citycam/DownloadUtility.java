package ru.ifmo.android_2015.citycam;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.citycam.ProgressCallback;

/**
 * Created by ruslanthakohov on 23/10/15.
 */
public class DownloadUtility {

    /**
     * Makes a network request to a given URL and writes the response in a given byte stream.
     *
     * @param downloadURL      the URL which to download file from
     * @param out              byte stream which to write file in
     * @param progressCallback optional callback for sending progress notifications.
                               Its method onProgressChanged is called synchronously
                               in the current thread.
     *
     * @throws IOException     In case of any errors during making the network request
                               or writing the response
     */
    static void downloadFile(URL downloadURL, ByteArrayOutputStream out, @Nullable ProgressCallback progressCallback) throws IOException {
        Log.d(tag, downloadURL.toString());

        HttpURLConnection connection = (HttpURLConnection) downloadURL.openConnection();

        InputStream in = null;

        try {
            
            //Check response code
            int responseCode = connection.getResponseCode();
            Log.d(tag, "HTTP response code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new FileNotFoundException("Unexpected HTTP response: " + responseCode
                + ", " + connection.getResponseMessage());
            }

            //Get the file size in bytes
            int contentLength = connection.getContentLength();
            Log.d(tag, "Content length: " + contentLength);

            byte[] buffer = new byte[128 * 1024]; //a temporary buffer for I/O of 128 kb
            int receivedBytes; //number of bytes received during the last read operation
            int receivedLength = 0; //total number of bytes received
            int progress = 0; //download progress in range from 0 to 100

            in = connection.getInputStream();

            while ((receivedBytes = in.read(buffer)) >= 0) {
                out.write(buffer, 0, receivedBytes);

                receivedLength += receivedBytes;
                if (contentLength > 0) {
                    int newProgress = 100 * receivedLength / contentLength;
                    if (newProgress > progress && progressCallback != null) {
                        Log.d(tag, "Downloaded " + newProgress + "% of " + contentLength + " bytes");
                        progressCallback.onProgressChanged(newProgress);
                    }
                    progress = newProgress;
                }
            }

            if (receivedLength != contentLength) {
                Log.w(tag, "Received " + receivedLength + " bytes, but expected " + contentLength);
            } else {
                Log.d(tag, "Received " + receivedLength + " bytes");
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(tag, "Failed to close HTTP input stream: " + e);
                }
            }
            connection.disconnect();
        }
    }
    
    static void downloadFile(URL downloadURL, ByteArrayOutputStream out) throws IOException {
        downloadFile(downloadURL, out, null);
    }
    

    private DownloadUtility() {}

    private static final String tag = "DownloadUtility";
}
