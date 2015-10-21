package ru.ifmo.android_2015.citycam.download;

public interface ProgressCallback {

    /**
     * Вызывается при изменении значения прогресса.
     * @param progress новое значение прогресса от 0 до 100.
     */
    void onProgressChanged(int progress);

    void onPostExecute(Long resultCode);
}
