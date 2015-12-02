package ru.ifmo.android_2015.citycam.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author creed
 * @date 06.11.15
 */
public class WebCam {
    private String user;
    private String title;
    @SerializedName("view_count")
    private int viewCount;
    private int active;
    @SerializedName("preview_url")
    private String previewUrl;
    @SerializedName("last_update")
    private long lastUpdate;

    public WebCam() {
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
