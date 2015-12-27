package ru.ifmo.android_2015.citycam;

import android.content.Context;

import java.io.File;
import java.io.IOException;

/**
 * Ìועמהû הכÿ נאבמעû ס פאיכאלט.
 */
final class FileUtils {
    
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