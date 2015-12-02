package ru.ifmo.android_2015.citycam.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author creed
 * @date 06.11.15
 */
public class WebCamsResult {
    private String status;

    public class WebCamsWrapper {
        private int count;
        private int page;
        @SerializedName("per_page")
        private int perPage;
        private List<WebCam> webcam;

        public WebCamsWrapper() {
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getPerPage() {
            return perPage;
        }

        public void setPerPage(int perPage) {
            this.perPage = perPage;
        }

        public List<WebCam> getWebcam() {
            return webcam;
        }

        public void setWebcam(List<WebCam> webcam) {
            this.webcam = webcam;
        }
    }

    private WebCamsWrapper webcams;

    public WebCamsResult() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public WebCamsWrapper getWebcams() {
        return webcams;
    }

    public void setWebcams(WebCamsWrapper webcams) {
        this.webcams = webcams;
    }
}
