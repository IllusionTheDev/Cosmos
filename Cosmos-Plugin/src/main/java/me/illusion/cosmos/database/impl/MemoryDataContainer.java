package me.illusion.cosmos.database.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.data.TemplateData;

/**
 * No clue why someone would want to use this, but here it is.
 *
 * @author Illusion
 */
public class MemoryDataContainer implements CosmosDataContainer {

    private final Map<String, TemplatedArea> templates = new ConcurrentHashMap<>();

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
    public CompletableFuture<Collection<String>> fetchAllTemplates() {
        return CompletableFuture.completedFuture(templates.keySet());
    }

    @Override
    public CompletableFuture<String> fetchTemplateSerializer(String name) {
        return templates.containsKey(name) ? CompletableFuture.completedFuture(templates.get(name).getSerializer().getName())
            : CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Collection<TemplateData>> fetchAllTemplateData() {
        List<TemplateData> list = new ArrayList<>();
        String containerName = getName();

        for (Map.Entry<String, TemplatedArea> entry : templates.entrySet()) {
            String templateName = entry.getKey();
            String serializerName = entry.getValue().getSerializer().getName();

            list.add(new TemplateData(templateName, serializerName, containerName));
        }

        return CompletableFuture.completedFuture(list);
    }

    @Override
    public boolean requiresCredentials() {
        return false;
    }
}
