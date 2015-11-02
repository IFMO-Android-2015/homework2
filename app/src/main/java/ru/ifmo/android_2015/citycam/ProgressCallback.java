package ru.ifmo.android_2015.citycam;

/**
 * Created by artem on 28.10.15.
 */
public interface ProgressCallback {

    /**
     * Вызывается при изменении значения прогресса.
     * @param progress новое значение прогресса от 0 до 100.
     */
    void onProgressChanged(int progress);
}