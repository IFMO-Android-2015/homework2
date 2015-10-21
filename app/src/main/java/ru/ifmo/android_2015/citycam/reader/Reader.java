package ru.ifmo.android_2015.citycam.reader;

import android.util.JsonReader;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Reader {

    private static Data[] data = null;
    public static int count, per_page, page;

    /**
     * Загружаем Json файл.
     */
    public static Data[] downloadJson(URL jsonUrl, int cam_number) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) jsonUrl.openConnection();
        InputStream in = null;

        try {
            int responseCode = conn.getResponseCode();
            Log.d(TAG, "URL: " + jsonUrl);
            Log.d(TAG, "Received HTTP response code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new FileNotFoundException("Unexpected HTTP response: " + responseCode
                        + ", " + conn.getResponseMessage());
            }

            in = conn.getInputStream();
            // Передаем поток для чтения
            count = readJsonStream(in);
            return data;
        } finally {
            // Закрываем все потоки и соедиениние
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close HTTP input stream: " + e, e);
                }
            }
            conn.disconnect();
        }
    }

    public static int readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readMessages(reader);
        } finally {
            reader.close();
        }
    }
    /**
     * Узнаем общее состояние камер.
     */
    private static int readMessages(JsonReader reader) throws IOException {
        reader.beginObject();
        reader.hasNext();
        String name = reader.nextName();
        Log.d(TAG, "name: " + name);
        if (name.equals("status"))
            if (reader.nextString().equals("ok")) {
                while (!name.equals("webcams")) {
                    reader.hasNext();
                    name = reader.nextName();
                    Log.d(TAG, "name: " + name);
                }
                return readCount(reader);
            }
        return 0;
    }
    /**
     * Узнаем количество камер, номер страницы и количество камер на странице.
     */
    private static int readCount(JsonReader reader) throws IOException {
        int count = 0;
        String name;

        reader.beginObject();

        while (reader.hasNext()) {
            name = reader.nextName();
            Log.d(TAG, "name: " + name);
            if (name.equals("count"))
                count = Integer.parseInt(reader.nextString());
            else if (name.equals("per_page"))
                per_page = reader.nextInt();
            else if (name.equals("page"))
                page = reader.nextInt();
            else if (!name.equals("webcam"))
                reader.skipValue();
            else
                break;
        }

        if (count < 1) {
            if (data != null)
                data = null;
            return 0;
        }
        if (data == null || data.length != count)
            data = new Data[count];
        if (count - (page - 1) * per_page > per_page)
            readData(reader, per_page);
        else
            readData(reader, count - (page - 1) * per_page);
        return count;
    }
    /**
     * Читаем данные по каждой камере и сохраняем в массив data.
     */
    public static void readData (JsonReader reader, int count) throws IOException {
        String user = "", name;
        URL url = null;
        long id = -1;

        reader.beginArray();
        for (int i = 0; i < count; i++) {
            reader.beginObject();
            while (reader.hasNext()) {
                name = reader.nextName();
                switch (name) {
                    case "user":
                        user = reader.nextString();
                        break;
                    case "webcamid":
                        id = reader.nextLong();
                        break;
                    case "preview_url":
                        url = new URL(reader.nextString());
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
                //Log.d(TAG, "name: " + name);
            }
            reader.endObject();
            data[i + per_page * (page - 1)] = new Data(url, id, user);
            Log.d(TAG, "i + per_page * (page - 1) : " + (i + per_page * (page - 1)));
        }
    }

    private static final String TAG = "Reader";
}
