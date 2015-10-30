package ru.ifmo.android_2015.citycam.model;

import java.net.URL;
import java.util.Date;

/**
 * Created by sandwwraith(@gmail.com)
 * ITMO University, 2015.
 */
public class WebCam {
    /**
     * Свойства камеры
     */
    private final URL url;
    private final String title;
    private final double rating;
    private final Date lastUpd;
    private final boolean existence;

    public WebCam(URL url, String title, Date lastUpd, double rating) {
        this.url = url;
        this.title = title;
        this.lastUpd = lastUpd;
        this.rating = rating;
        this.existence = true;
    }

    public WebCam() {
        this.url = null;
        this.title = null;
        this.lastUpd = new Date();
        this.rating = 0.0;
        this.existence = false;
    }

    public URL getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public double getRating() {
        return rating;
    }

    public Date getLastUpd() {
        return lastUpd;
    }

    /**
     * Флаг false, когда камер в запрошенной локации нет. Добавлено, чтобы отличать ошибку от отсутствия
     */
    public boolean exists() {
        return existence;
    }
}
