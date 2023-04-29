package me.illusion.cosmos.database.impl;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.TemplatedArea;
import org.bson.Document;
import org.bukkit.configuration.ConfigurationSection;

public class MongoDataContainer implements CosmosDataContainer {

    private final List<CompletableFuture<?>> futures = new ArrayList<>();
    private MongoClient mongoClient;
    private MongoCollection<Document> templatesCollection;

    private final CosmosPlugin plugin;

    public MongoDataContainer(CosmosPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Boolean> enable(ConfigurationSection section) {
        return CompletableFuture.supplyAsync(() -> {
            String ip = section.getString("ip");
            int port = section.getInt("port");
            String authsource = section.getString("auth-source");
            String username = section.getString("username");
            String password = section.getString("password");

            String database = section.getString("database", "cosmos");
            String collectionName = section.getString("collection", "cosmos_templates");

            mongoClient = MongoClients.create(
                new ConnectionString("mongodb://" + username + ":" + password + "@" + ip + ":" + port + "/?authSource=" + authsource));

            try {
                templatesCollection = mongoClient.getDatabase(database).getCollection(collectionName);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<TemplatedArea> fetchTemplate(String name) {
        CompletableFuture<TemplatedArea> future = new CompletableFuture<>();
        futures.add(future);
        future.thenRun(() -> futures.remove(future));

        CompletableFuture<Void> fetch = CompletableFuture.runAsync(() -> {
            Document document = templatesCollection.find(new Document("name", name)).first();
            if (document == null) {
                future.complete(null);
                return;
            }

            byte[] data = document.get("data", byte[].class);
            String serializer = document.getString("serializer");

            CosmosSerializer cosmosSerializer = plugin.getSerializerRegistry().get(serializer);

            if (cosmosSerializer == null) {
                plugin.getLogger().warning("Could not find serializer " + serializer + " for template " + name);
                future.complete(null);
                return;
            }

            cosmosSerializer.deserialize(data).thenAccept(future::complete);
        });

        futures.add(fetch);
        fetch.thenRun(() -> futures.remove(fetch));

        return future;
    }

    @Override
    public CompletableFuture<Void> saveTemplate(String name, TemplatedArea area) {
        CompletableFuture<Void> future = area.getSerializer().serialize(area).thenAccept(binary -> {
            Document document = new Document("name", name)
                .append("data", binary)
                .append("serializer", area.getSerializer().getName());

            templatesCollection.insertOne(document);
        });

        futures.add(future);
        future.thenRun(() -> futures.remove(future));

        return future;
    }

    @Override
    public CompletableFuture<Void> deleteTemplate(String name) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> templatesCollection.deleteOne(new Document("name", name)));

        futures.add(future);
        future.thenRun(() -> futures.remove(future));

        return future;
    }

    @Override
    public CompletableFuture<Void> flush() {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public String getName() {
        return "mongodb";
    }

    @Override
    public boolean requiresCredentials() {
        return true;
    }
}
