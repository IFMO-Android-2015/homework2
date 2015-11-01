package ru.ifmo.android_2015.citycam.webcams;

/**
 * Created by baba_beda on 11/1/15.
 */
public class WebcamData {
    private String bitmapURL;
    private String user;
    private String title;
    private long time;

    public WebcamData() {
        bitmapURL = "";
        user = "";
        title = "";
        time = 0L;
    }

    public String getBitmapURL() {
        return bitmapURL;
    }

    public void setBitmapURL(String bitmapURL) {
        this.bitmapURL = bitmapURL;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
