package ru.ifmo.android_2015.citycam.file;

import android.content.Context;

import java.io.File;
import java.io.IOException;


public class CreateFile {
    /**
     * Сохраняем файл (или проверяем что он существует) в котором будем хранить изображение.
     */
    public static File createTempExternalFile(Context context, String extension) throws IOException {
        File dir = new File(context.getExternalFilesDir(null), "cityImage");
        if (dir.exists() && !dir.isDirectory()) {
            throw new IOException("Not a directory: " + dir);
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + dir);
        }
        if (extension == null) {
            extension = ".tmp";
        }
        File result;
        result = new File(dir, "cityCam" + extension);
        if (!result.exists())
            result.createNewFile();
        return result;
    }
}
