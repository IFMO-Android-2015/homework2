package ru.ifmo.android_2015.citycam.reader;

import java.net.URL;
/**
 * Контейнер для хранения данных камер.
 */
public class Data {

    public URL url;
    public long id;
    public String user;

    public Data(URL url, long id, String user) {
        this.url = url;
        this.id = id;
        this.user = user;
    }
}
