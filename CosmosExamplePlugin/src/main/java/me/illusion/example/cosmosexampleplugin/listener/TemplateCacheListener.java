package me.illusion.example.cosmosexampleplugin.listener;

import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.event.CosmosTemplateMigrateEvent;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.example.cosmosexampleplugin.CosmosExamplePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TemplateCacheListener implements Listener {

    private final CosmosExamplePlugin plugin;

    public TemplateCacheListener(CosmosExamplePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onMigrate(CosmosTemplateMigrateEvent event) {
        String templateId = event.getTemplateId();
        TemplatedArea template = event.getTemplate();

        CosmosDataContainer targetContainer = event.getTargetContainer();
        String targetContainerName = targetContainer.getName();

        if (!plugin.getCosmosPlugin().getContainerRegistry().getDefaultContainer().getName().equalsIgnoreCase(targetContainerName)) {
            return; // We want to see if the target container is the default container
        }

        if (!templateId.equalsIgnoreCase("skyblock")) {
            return; // We want to see if the template is the skyblock template
        }

        plugin.getCosmosPlugin().getTemplateCache()
            .register(templateId, template); // Register the template to the cache, that way we don't need to restart the server
    }

}
