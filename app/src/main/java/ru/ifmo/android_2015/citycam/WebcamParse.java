package ru.ifmo.android_2015.citycam;

import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ru.ifmo.android_2015.citycam.model.Webcam;

/**
 * Created by andrey on 10.11.15.
 */
public class WebcamParse {
    private String title = "";
    private String name = "";
    private String preview_url = "";
    public Webcam parseJson(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in,"UTF-8"));
        try {
            return parse(reader);
        } catch (Exception e) {
            Log.w(TAG, "error");
        }  finally {
            reader.close();
        }
        return null;
    }

    public Webcam parse(JsonReader reader) throws IOException {
        reader.beginObject();

        while (reader.hasNext()) {
            name = reader.nextName();
            if (name.equals("webcams")) {
                reader.beginObject();
                while (reader.hasNext()) {
                    name = reader.nextName();
                    if (name.equals("webcam")) {
                        reader.beginArray();
                        reader.beginObject();
                        while (reader.hasNext()) {
                            if (!title.equals("") && !preview_url.equals("")) {
                                return new Webcam(title, preview_url);
                            }
                            name = reader.nextName();
                            if (name.equals("preview_url")) {
                                preview_url = reader.nextString();
                            } else if (name.equals("title")) {
                                title = reader.nextString();
                            }  else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
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
        return null;
    }

    private static final String TAG = "CityCam";
}
