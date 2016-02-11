package ru.ifmo.android_2015.citycam;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dan) on 09.11.2015. .
 */
final class CamParceUtils {
    CamParceUtils() {}
    public Cam readJSONStream(URL downloadUrl) throws IOException {

        Log.d(TAG, "Start downloading url: " + downloadUrl);

        HttpURLConnection conn = (HttpURLConnection) downloadUrl.openConnection();

        InputStream in = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        JsonReader jsonReader = null;
        try{
            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Received HTTP response code: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new FileNotFoundException(conn.getResponseMessage());
            }

            in = conn.getInputStream();
            inputStreamReader = new InputStreamReader(in);
            bufferedReader = new BufferedReader(inputStreamReader);
            jsonReader = new JsonReader(inputStreamReader);

            try {
                return readCam(jsonReader);
            }
            finally {
                jsonReader.close();
            }

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    //failed to close input stream
                    Log.e(TAG, "Failed to close HTTP input stream: " + e, e);
                }
            }
            conn.disconnect();
        }
    }

    private Cam readCam(JsonReader reader) throws IOException {
        String status = null;
        Cam result = new Cam();
        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();
            if (field.equals("status") && (reader.peek() != JsonToken.NULL)) {
                status = reader.nextString();
            } else if (field.equals("webcams")) {
                reader.beginObject();
                while (reader.hasNext()) {
                    field = reader.nextName();
                    if (field.equals("webcam")) {
                        reader.beginArray();
                        while (reader.hasNext()) {
                            reader.beginObject();
                            while (reader.hasNext()) {
                                String camField = reader.nextName();
                                if (camField.equals("title") && (reader.peek() != JsonToken.NULL)) {
                                    result.title = reader.nextString();
                                } else if (camField.equals("preview_url") && (reader.peek() != JsonToken.NULL)) {
                                    result.preview_url = reader.nextString();
                                } else {
                                    reader.skipValue();
                                }
                            }
                            reader.endObject();
                        }
                        reader.endArray();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return result;
    }

    private static final String TAG = "parceJSON";
}
