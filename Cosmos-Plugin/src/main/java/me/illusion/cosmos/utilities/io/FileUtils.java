package me.illusion.cosmos.utilities.io;

import java.io.File;

public class FileUtils {

    private FileUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static void deleteDirectory(File directory) {
        if (!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }

        directory.delete();
    }

}
