package ru.ifmo.android_2015.citycam.Utils;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import ru.ifmo.android_2015.citycam.webcams.Webcam;

public final class JSONUtil {
    public static Webcam getWebcam(InputStream inputStream) throws IOException{
        Webcam webcam = null;
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("webcams")){
                reader.beginObject();
                while (reader.hasNext()){
                    if (reader.nextName().equals("webcam")){
                        reader.beginArray();
                        if (reader.hasNext()){
                            reader.beginObject();
                            while (reader.hasNext()){
                                if (reader.nextName().equals("preview_url")){
                                    webcam = new Webcam(new URL(reader.nextString()));
                                    break;
                                } else {
                                    reader.skipValue();
                                }
                            }
                        }
                    } else {
                        reader.skipValue();
                    }
                }
            } else {
                reader.skipValue();
            }
        }
        reader.close();
        return webcam;
    }
}
