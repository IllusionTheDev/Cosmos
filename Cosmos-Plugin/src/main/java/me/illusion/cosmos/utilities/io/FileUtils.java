package me.illusion.cosmos.utilities.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.bukkit.plugin.java.JavaPlugin;

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

    public static void copyBuiltInResources(JavaPlugin plugin,
        File jarFile) { // the jarFile must be fetched through JavaPlugin#getFile, as it's a protected method
        // Copy all resources from the jar to the data folder, except plugin.yml and code
        // Open the jar file as a zip
        try (JarFile jar = new JarFile(jarFile)) {
            // Get the entries in the jar file
            Enumeration<JarEntry> entries = jar.entries();

            // Iterate over the entries
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                // Make sure to not copy classes (including dependencies), we only want resources
                if (name.endsWith(".class")) {
                    continue;
                }

                // skip meta-inf
                if (name.startsWith("META-INF/")) {
                    continue;
                }

                // Skip plugin.yml
                if (name.equals("plugin.yml")) {
                    continue;
                }

                // If the entry is a directory, and the directory doesn't contain code, create it
                if (name.endsWith("/")) {
                    if (isCodeDirectory(name, jar.entries())) {
                        continue;
                    }

                    new File(plugin.getDataFolder(), name).mkdirs();
                    continue;
                }

                // The entry is a file, so copy it
                try (InputStream in = jar.getInputStream(entry)) {
                    File file = new File(plugin.getDataFolder(), name);

                    if (file.exists()) {
                        continue;
                    }

                    Files.copy(in, file.toPath());
                } catch (IOException ignored) {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isCodeDirectory(String name, Enumeration<JarEntry> dir) {
        while (dir.hasMoreElements()) {
            JarEntry entry = dir.nextElement();
            String entryName = entry.getName();

            if (entryName.startsWith(name) && entryName.endsWith(".class")) {
                return true;
            }
        }

        return false;
    }

}
