package me.illusion.cosmos.hook;

import com.grinderwolf.swm.plugin.SWMPlugin;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.database.hook.SlimeLoader;
import me.illusion.cosmos.serialization.impl.SlimeSerializer;

public class SlimeCompatibility {

    public static void init(CosmosPlugin plugin) {
        SWMPlugin swmPlugin = SWMPlugin.getInstance();

        CosmosDataContainer container = plugin.getContainerRegistry().getDefaultContainer();

        swmPlugin.registerLoader("cosmos", new SlimeLoader(container));
        plugin.getSerializerRegistry().register(new SlimeSerializer(container));
    }

}
