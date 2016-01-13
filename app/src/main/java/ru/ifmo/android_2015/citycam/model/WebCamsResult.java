package ru.ifmo.android_2015.citycam.model;

import java.util.List;

/**
 * @author Andreikapolin
 * @date 12.01.16
 */
public class WebCamsResult {

    private WebCamsWrapper webcams;

    public class WebCamsWrapper {

        private List<WebCam> webcam;

        public WebCamsWrapper() {}

        public List<WebCam> getWebcam() {
            return webcam;
        }

        public void setWebcam(List<WebCam> webcam) {
            this.webcam = webcam;
        }
    }


    public WebCamsResult() {}

    public WebCamsWrapper getWebcams() {
        return webcams;
    }

    public void setWebcams(WebCamsWrapper webcams) {
        this.webcams = webcams;
    }
}
