package me.illusion.cosmos.database.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.template.TemplatedArea;

/**
 * No clue why someone would want to use this, but here it is.
 *
 * @author Illusion
 */
public class MemoryDataContainer implements CosmosDataContainer {

    private final Map<String, TemplatedArea> templates = new ConcurrentHashMap<>();
    private final Map<String, byte[]> binaryTemplates = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<TemplatedArea> fetchTemplate(String name) {
        return CompletableFuture.completedFuture(templates.get(name));
    }

    @Override
    public CompletableFuture<Void> saveTemplate(String name, TemplatedArea area) {
        templates.put(name, area);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteTemplate(String name) {
        templates.remove(name);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> flush() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "memory";
    }

    @Override
    public CompletableFuture<byte[]> fetchBinaryTemplate(String name) {
        return CompletableFuture.completedFuture(binaryTemplates.get(name));
    }

    @Override
    public CompletableFuture<Void> saveBinaryTemplate(String name, byte[] data) {
        binaryTemplates.put(name, data);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<String>> fetchTemplateNames() {
        return CompletableFuture.completedFuture(List.copyOf(templates.keySet()));
    }

    @Override
    public boolean requiresCredentials() {
        return false;
    }
}
