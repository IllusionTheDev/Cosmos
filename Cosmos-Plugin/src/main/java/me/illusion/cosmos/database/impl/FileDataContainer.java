package me.illusion.cosmos.database.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.data.TemplateData;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileDataContainer implements CosmosDataContainer {

    private final CosmosPlugin plugin;
    private final File worldContainer;

    private final List<CompletableFuture<?>> runningFutures = new ArrayList<>();

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
    public CompletableFuture<Collection<String>> fetchAllTemplates() {
        CompletableFuture<Collection<String>> future = new CompletableFuture<>();

        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
            Collection<String> templates = new ArrayList<>();

            File[] files = worldContainer.listFiles();

            if (files == null) {
                future.complete(templates);
                return;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    templates.add(file.getName());
                }
            }

            future.complete(templates);
        });

        registerFuture(task);
        registerFuture(future);
        return future;
    }

    @Override
    public CompletableFuture<String> fetchTemplateSerializer(String name) {
        CompletableFuture<String> task = CompletableFuture.supplyAsync(() -> {
            File templateFolder = new File(worldContainer, name);

            if (!templateFolder.exists()) {
                return null;
            }

            File metadataFile = new File(templateFolder, "metadata.yml");

            if (!metadataFile.exists()) {
                return null;
            }

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(metadataFile);
            return yaml.getString("serializer");
        });

        registerFuture(task);
        return task;
    }

    @Override
    public CompletableFuture<Collection<TemplateData>> fetchAllTemplateData() {
        CompletableFuture<Collection<TemplateData>> future = new CompletableFuture<>();

        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
            Collection<TemplateData> templates = new ArrayList<>();

            File[] files = worldContainer.listFiles();

            if (files == null) {
                future.complete(templates);
                return;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    File metadataFile = new File(file, "metadata.yml");

                    if (!metadataFile.exists()) {
                        continue;
                    }

                    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(metadataFile);
                    String serializer = yaml.getString("serializer");

                    templates.add(new TemplateData(file.getName(), serializer, getName()));
                }
            }

            future.complete(templates);
        });

        registerFuture(task);
        registerFuture(future);
        return future;
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

            byte[] dataContents = readFully(dataFile);
            cosmosSerializer.deserialize(dataContents).thenAccept(future::complete);
        });

        registerFuture(task);
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
        });

        registerFuture(task);
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
        });

        registerFuture(task);
        return task;
    }

    @Override
    public CompletableFuture<Void> flush() {
        return CompletableFuture.allOf(runningFutures.toArray(new CompletableFuture[0]));
    }

    private <T> CompletableFuture<T> registerFuture(CompletableFuture<T> future) {
        future.thenRun(() -> runningFutures.remove(future));
        future.exceptionally(throwable -> {
            runningFutures.remove(future);
            throwable.printStackTrace();
            return null;
        });

        runningFutures.add(future);
        return future;
    }

    private byte[] readFully(File file) {
        // Read the file's full contents
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
