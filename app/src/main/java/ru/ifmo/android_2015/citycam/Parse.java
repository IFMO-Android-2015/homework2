package ru.ifmo.android_2015.citycam;

import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ru.ifmo.android_2015.citycam.model.Cam;

/**
 * Created by Лиза on 18.11.2015.
 */
public class Parse {
    private static final String TAG = ":(";
    private String name = "";
    private String title = "";
    private String preview_url = "";

    public Cam parseJson(InputStream input) throws IOException {
        JsonReader info = new JsonReader(new InputStreamReader(input,"UTF-8"));
        try {
            return parse(info);
        } catch (Exception e) {
            Log.w(TAG, "error");
        }  finally {
            info.close();
        }
        return null;
    }

    public Cam parse(JsonReader info) throws IOException {
        info.beginObject();
        while (info.hasNext()) {
            name = info.nextName();
            if (name.equals("webcams")) {
                info.beginObject();
                while (info.hasNext()) {
                    name = info.nextName();
                    if (name.equals("webcam")) {
                        info.beginArray();
                        info.beginObject();
                        while (info.hasNext()) {
                            if (!title.equals("") && !preview_url.equals("")) {
                                return new Cam(title, preview_url);
                            }
                            name = info.nextName();
                            if (name.equals("preview_url")) {
                                preview_url = info.nextString();
                            } else if (name.equals("title")) {
                                title = info.nextString();
                            }  else {
                                info.skipValue();
                            }
                        }
                        info.endObject();
                        info.endArray();
                    } else {
                        info.skipValue();
                    }
                }
                info.endObject();
            } else {
                info.skipValue();
            }
        }
        info.endObject();
        return null;
    }
}
