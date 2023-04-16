package me.illusion.cosmos;

import me.illusion.cosmos.serialization.CosmosSerializerRegistry;
import org.bukkit.plugin.java.JavaPlugin;

public final class CosmosPlugin extends JavaPlugin {

    private CosmosSerializerRegistry serializerRegistry;

    @Override
    public void onEnable() {
        // Plugin startup logic
        serializerRegistry = new CosmosSerializerRegistry();

        registerDefaults();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void registerDefaults() {
        serializerRegistry.registerDefaultSerializers();
    }
}
