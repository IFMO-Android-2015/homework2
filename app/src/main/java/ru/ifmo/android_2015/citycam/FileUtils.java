package ru.ifmo.android_2015.citycam;

import android.content.Context;

import java.io.File;
import java.io.IOException;

/**
 * Методы для работы с файлами.
 */
final class FileUtils {

    /**
     * Создает временный пустой файл в папке приложения в External Storage
     * Дтректория: /sdcard/Android/data/<application_package_name>/files/tmp
     *
     * Имя файла генерируется случайным образом, к нему можно добавить расширение. Файл никак
     * автоматически не удаляется -- получатель сам должен позаботиться об удалении после
     * использования.
     *
     * @param context   контекст приложения
     * @param extension расширение, которое будет добавлено в конце имени файла.
     *
     * @return  новый пустой файл
     *
     * @throws IOException  в случае ошибки создания файла.
     */
    static File createTempExternalFile(Context context, String extension) throws IOException {
        File dir = new File(context.getExternalFilesDir(null), "tmp");
        if (dir.exists() && !dir.isDirectory()) {
            throw new IOException("Not a directory: " + dir);
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + dir);
        }
        return File.createTempFile("tmp", extension, dir);
    }

    private FileUtils() {}
}
