package ru.ifmo.android_2015.citycam.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Andreikapolin
 * @date 12.01.16
 */
public class WebCam {
    private String title;

    @SerializedName("view_count")
    private int viewCount;

    @SerializedName("preview_url")
    private String previewUrl;

    @SerializedName("last_update")
    private long lastUpdate;

    public WebCam() {}

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
