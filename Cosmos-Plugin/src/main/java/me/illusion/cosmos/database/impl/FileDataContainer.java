package me.illusion.cosmos.database.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.TemplatedArea;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileDataContainer implements CosmosDataContainer {

    private final CosmosPlugin plugin;
    private final File worldContainer;

    private final List<CompletableFuture<?>> runningTasks = new ArrayList<>();

    public FileDataContainer(CosmosPlugin plugin) {
        this.plugin = plugin;

        File cosmosFolder = plugin.getDataFolder();

        worldContainer = new File(cosmosFolder, "templates");

        createFolder(worldContainer);
    }

    private void createFolder(File folder) {
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public CompletableFuture<TemplatedArea> fetchTemplate(String name) {
        CompletableFuture<TemplatedArea> future = new CompletableFuture<>();

        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> { // there's probably a better way to do this
            File templateFolder = new File(worldContainer, name);

            if (!templateFolder.exists()) {
                System.out.println("Template folder " + name + " does not exist");
                future.complete(null);
                return;
            }

            File dataFile = new File(templateFolder, "data.cosmos");
            File metadataFile = new File(templateFolder, "metadata.yml");

            if (!dataFile.exists() || !metadataFile.exists()) {
                System.out.println("Template folder " + name + " is missing files");
                future.complete(null);
                return;
            }

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(metadataFile);
            String serializer = yaml.getString("serializer");

            CosmosSerializer cosmosSerializer = plugin.getSerializerRegistry().get(serializer);

            if (cosmosSerializer == null) {
                plugin.getLogger().warning("Could not find serializer " + serializer + " for template " + name);
                future.complete(null);
                return;
            }

            System.out.println("Loading template " + name + " with serializer " + serializer);
            byte[] dataContents = readFully(dataFile);

            // merge these futures without joining
            cosmosSerializer.deserialize(dataContents).thenAccept(future::complete);
        }).exceptionally((e) -> {
            e.printStackTrace();
            future.completeExceptionally(e);
            return null;
        });

        task.thenRun(() -> runningTasks.remove(task));
        future.thenRun(() -> {
            runningTasks.remove(future);
            System.out.println("Completed template " + name);
        }).exceptionally((e) -> {
            e.printStackTrace();
            return null;
        });

        runningTasks.add(task);
        runningTasks.add(future);

        return future;
    }

    @Override
    public CompletableFuture<Void> saveTemplate(String name, TemplatedArea area) {
        CompletableFuture<Void> task = area.getSerializer().serialize(area).thenAccept((contents) -> {
            File templateFolder = new File(worldContainer, name);

            createFolder(templateFolder);

            File dataFile = new File(templateFolder, "data.cosmos");
            File metadataFile = new File(templateFolder, "metadata.yml");

            YamlConfiguration yaml = new YamlConfiguration();
            yaml.set("serializer", area.getSerializer().getName());

            try {
                yaml.save(metadataFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // write contents to dataFile
            writeFully(dataFile, contents);
        }).exceptionally((e) -> {
            e.printStackTrace();
            return null;
        });

        task.thenRun(() -> runningTasks.remove(task));
        runningTasks.add(task);

        return task;
    }

    @Override
    public CompletableFuture<Void> deleteTemplate(String name) {
        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
            File templateFolder = new File(worldContainer, name);

            if (!templateFolder.exists()) {
                return;
            }

            File dataFile = new File(templateFolder, "data.cosmos");
            File metadataFile = new File(templateFolder, "metadata.yml");

            if (!dataFile.exists() || !metadataFile.exists()) {
                return;
            }

            dataFile.delete();
            metadataFile.delete();

            templateFolder.delete();
        }).exceptionally((e) -> {
            e.printStackTrace();
            return null;
        });

        task.thenRun(() -> runningTasks.remove(task));
        runningTasks.add(task);

        return task;
    }

    @Override
    public CompletableFuture<Void> flush() {
        return CompletableFuture.allOf(runningTasks.toArray(new CompletableFuture[0]));
    }

    private byte[] readFully(File file) {
        // Read the file's full contents
        byte[] data = new byte[(int) file.length()];

        try (InputStream in = new FileInputStream(file)) {
            int read = 0;
            while (read < data.length) {
                int r = in.read(data, read, data.length - read);
                if (r == -1) {
                    break;
                }

                read += r;
            }
        } catch (IOException ex) {
            return null;
        }

        return data;
    }

    private void writeFully(File file, byte[] data) {
        // Write the file's full contents
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(data);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean requiresCredentials() {
        return false;
    }
}
