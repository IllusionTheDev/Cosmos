package me.illusion.example;

import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.grid.CosmosGrid;
import me.illusion.cosmos.template.grid.impl.WorldPerAreaGrid;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import me.illusion.cosmos.utilities.hook.WorldEditUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ExamplePlugin extends JavaPlugin {

    private CosmosPlugin cosmosPlugin;
    private CosmosGrid grid;

    private CosmosDataContainer templateContainer;

    @Override
    public void onEnable() {
        cosmosPlugin = (CosmosPlugin) Bukkit.getPluginManager().getPlugin("Cosmos");
        grid = WorldPerAreaGrid.builder().build();

        cosmosPlugin.getGridRegistry().register(grid);

        templateContainer = cosmosPlugin.getContainerRegistry().getContainer("file");

        registerDefaults();
    }

    private void registerDefaults() {
        cosmosPlugin.getTemplateCache().register("arena1", templateContainer.fetchTemplate("arena1"));
        cosmosPlugin.getTemplateCache().register("arena2", templateContainer.fetchTemplate("arena2"));
    }

    public void createArena(Player admin, String name) {
        Cuboid selection = WorldEditUtils.getPlayerSelection(admin);
        Location center = admin.getLocation();

        CosmosSerializer serializer = cosmosPlugin.getSerializerRegistry().get("worldedit");

        serializer.createArea(selection, center).thenAccept(template -> {
            templateContainer.saveTemplate(name, template);
            cosmosPlugin.getTemplateCache().register(name, template);
        });
    }

    public void createArena(Player one, Player two) {
        TemplatedArea template = cosmosPlugin.getTemplateCache().get("arena1");

        grid.paste(template).thenAccept(arena -> {
            Location center = arena.getPasteLocation();

            one.teleport(center.clone().add(-10, 0, 0));
            two.teleport(center.clone().add(10, 0, 0));

            cosmosPlugin.getPasteCache().register("arena-" + one.getName() + "-" + two.getName(), arena);
        });
    }

    public void removeArena(Player one, Player two) {
        cosmosPlugin.getPasteCache().get("arena-" + one.getName() + "-" + two.getName()).unload();
    }

    public void deleteArenaTemplate(String name) {
        templateContainer.deleteTemplate(name);
        cosmosPlugin.getTemplateCache().unregister(name);
    }

    public void removeAllArenas() {
        grid.unloadAll();
    }
}
