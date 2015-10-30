package ru.ifmo.android_2015.citycam.save;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import ru.ifmo.android_2015.citycam.CityCamActivity;
import ru.ifmo.android_2015.citycam.reader.Data;
/**
 * Контейнер для хранения данных при перевороте экрана.
 */
public class Container {

    public CityCamActivity.DownloadFileTask downloadTask;
    public CityCamActivity.DownloadJsonTask downloadJsonTask;
    public int current_cam, page, per_page, all_cam;
    public Data[] data;

    public Container ( CityCamActivity.DownloadJsonTask downloadJsonTask,
                       CityCamActivity.DownloadFileTask downloadTask, Data[] data,
                       int current_cam, int page, int per_page, int all_cam) {
        this.downloadJsonTask = downloadJsonTask;
        this.downloadTask = downloadTask;
        this.data = data;
        this.current_cam = current_cam;
        this.page = page;
        this.per_page = per_page;
        this.all_cam = all_cam;
    }
}
