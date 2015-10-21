package ru.ifmo.android_2015.citycam.file;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import ru.ifmo.android_2015.citycam.reader.Data;


public class CreateFile {

    public Data[] data;

    public static File createTempExternalFile(Context context, String city, int count, String extension) throws IOException {
        File dir = new File(context.getExternalFilesDir(null), "cityImage");
        if (dir.exists() && !dir.isDirectory()) {
            throw new IOException("Not a directory: " + dir);
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + dir);
        }
        if (city.length() < 3) {
            throw new IllegalArgumentException("prefix must be at least 3 characters");
        }
        if (extension == null) {
            extension = ".tmp";
        }
        File result;
        result = new File(dir, city + "_" + count + extension);
        if (!result.exists())
            result.createNewFile();
        return result;
    }
}
