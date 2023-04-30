package me.illusion.cosmos.database.hook;

import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import java.io.IOException;
import java.util.List;
import me.illusion.cosmos.database.CosmosDataContainer;

public class SlimeLoader implements com.grinderwolf.swm.api.loaders.SlimeLoader {

    private final CosmosDataContainer backingContainer;

    public SlimeLoader(CosmosDataContainer backingContainer) {
        this.backingContainer = backingContainer;
    }

    @Override
    public byte[] loadWorld(String s, boolean b) throws UnknownWorldException, WorldInUseException, IOException {
        return backingContainer.fetchBinaryTemplate(s).join();
    }

    @Override
    public boolean worldExists(String s) throws IOException {
        return backingContainer.fetchBinaryTemplate(s).join() != null;
    }

    @Override
    public List<String> listWorlds() throws IOException {
        return backingContainer.fetchTemplateNames().join();
    }

    @Override
    public void saveWorld(String s, byte[] bytes, boolean b) throws IOException {
        backingContainer.saveBinaryTemplate(s, bytes).join();
    }

    @Override
    public void unlockWorld(String s) throws UnknownWorldException, IOException {

    }

    @Override
    public boolean isWorldLocked(String s) throws UnknownWorldException, IOException {
        return false;
    }

    @Override
    public void deleteWorld(String s) throws UnknownWorldException, IOException {
        backingContainer.deleteTemplate(s).join();
    }
}
