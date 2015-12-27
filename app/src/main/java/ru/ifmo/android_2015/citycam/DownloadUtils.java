package ru.ifmo.android_2015.citycam;

import android.support.annotation.Nullable;
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
 * ������ ��� ���������� ������.
 */
final class DownloadUtils {

    /**
     * ��������� ������� ������ ��� ���������� �����, � ��������� ����� � ��������� ����.
     * ����� ���������� ������������ �������� ���������� �����.
     *
     * @throws IOException  � ������ ������ ���������� �������� ������� ��� ������ �����.
     */
    static void downloadFile(String downloadUrl,
                             File destFile,
                             @Nullable ProgressCallback progressCallback) throws IOException {
        Log.d(TAG, "Start downloading url: " + downloadUrl);
        Log.d(TAG, "Saving to file: " + destFile);

        // ��������� ������ �� ���������� URL. ��������� �� ���������� ������ http:// ��� https://
        // URL ��� ����������, �� �������� ��������� � HttpURLConnection. � ������ URL � ������
        // ������, ����� ������.
        HttpURLConnection conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
        InputStream in = null;
        OutputStream out = null;

        try {

            // ��������� HTTP ��� ������. ������� ������ ����� 200 (��).
            // ��������� ���� ������� �������.
            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Received HTTP response code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new FileNotFoundException("Unexpected HTTP response: " + responseCode
                        + ", " + conn.getResponseMessage());
            }

            // ������ ������ �����, ������� �� ���������� �������
            // (�������� � ������ � HTTP ��������� Content-Length)
            int contentLength = conn.getContentLength();
            Log.d(TAG, "Content Length: " + contentLength);

            // ������� ��������� ������ ��� I/O �������� �������� 128��
            byte [] buffer = new byte[1024 * 128];

            // ������ ���������� ������ � ������
            int receivedBytes;
            // ������� ���� ����� �������� (� ��������).
            int receivedLength = 0;
            // �������� ���������� �� 0 �� 100
            int progress = 0;

            // �������� ������ �����
            in = conn.getInputStream();
            // � ��������� ���� ��� ������
            out = new FileOutputStream(destFile);

            // � ����� ������ ������ �������� � ������, � �� ������� ����� � ����.
            // ����������� �� �������� ����� ����� -- in.read(buffer) ���������� -1
            while ((receivedBytes = in.read(buffer)) >= 0) {
                out.write(buffer, 0, receivedBytes);
                receivedLength += receivedBytes;

                if (contentLength > 0) {
                    int newProgress = 100 * receivedLength / contentLength;
                    if (newProgress > progress && progressCallback != null) {
                        Log.d(TAG, "Downloaded " + newProgress + "% of " + contentLength + " bytes");
                        progressCallback.onProgressChanged(newProgress);
                    }
                    progress = newProgress;
                }
            }

            if (receivedLength != contentLength) {
                Log.w(TAG, "Received " + receivedLength + " bytes, but expected " + contentLength);
            } else {
                Log.d(TAG, "Received " + receivedLength + " bytes");
            }

        } finally {
            // ��������� ��� ������ � �����������
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close HTTP input stream: " + e, e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close file: " + e, e);
                }
            }
            conn.disconnect();
        }
    }

    static void downloadFile(String downloadUrl, File destFile) throws IOException {
        downloadFile(downloadUrl, destFile, null /*progressCallback*/);
    }

    private static final String TAG = "Download";


    private DownloadUtils() {}
}