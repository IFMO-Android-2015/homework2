package ru.ifmo.android_2015.citycam;

/**
 * Created by Anstanasia on 23.12.2015.
 */
public class Webcam {
    public String title;
    public String user;
    public long viewCount;
    public String previewURL;

    public Webcam(String title, String user, long viewCount, String previewURL) {
        this.title = title;
        this.user = user;
        this.viewCount = viewCount;
        this.previewURL = previewURL;
    }
}
