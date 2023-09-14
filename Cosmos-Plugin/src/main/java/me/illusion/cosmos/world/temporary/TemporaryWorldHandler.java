package me.illusion.cosmos.world.temporary;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import me.illusion.cosmos.utilities.concurrency.MainThreadExecutor;
import me.illusion.cosmos.utilities.io.FileUtils;
import me.illusion.cosmos.utilities.time.Time;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class TemporaryWorldHandler {

    // To avoid the risk of corruption, we'll just create an empty "temporary.cosmos" file in the world folder
    // If this file exists, we'll assume the world is temporary and delete it when the server starts, or a deletion is requested

    public boolean isTemporary(String worldName) {
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);

        if (!worldFolder.exists() || !worldFolder.isDirectory()) {
            return false;
        }

        File cosmosFile = new File(worldFolder, "temporary.cosmos");

        return cosmosFile.exists();
    }

    public boolean isTemporary(World world) {
        return isTemporary(world.getName());
    }

    public void markTemporary(String worldName) {
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);

        if (!worldFolder.exists() || !worldFolder.isDirectory()) {
            System.out.println("World " + worldName + " does not exist, or is not a directory");
            return;
        }

        File cosmosFile = new File(worldFolder, "temporary.cosmos");

        try {
            cosmosFile.createNewFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void markTemporary(World world) {
        markTemporary(world.getName());
    }

    public World createWorld(WorldCreator creator) {
        World world = creator.createWorld();
        markTemporary(world);

        return world;
    }

    public CompletableFuture<Void> deleteWorld(String name, boolean shutdown) {
        File worldFolder = new File(Bukkit.getWorldContainer(), name);

        if (!worldFolder.exists() || !worldFolder.isDirectory()) {
            return CompletableFuture.completedFuture(null);
        }

        if (shutdown) {
            FileUtils.deleteDirectory(worldFolder);
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                FileUtils.deleteDirectory(worldFolder);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> unloadAndDelete(World world, Time expectedUnloadDelay) {
        return unloadAndDelete(world, expectedUnloadDelay, false);
    }

    public CompletableFuture<Void> unloadAndDelete(World world, Time expectedUnloadDelay, boolean shutdown) {
        if (!Bukkit.isPrimaryThread()) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            CompletableFuture.runAsync(() -> unloadAndDelete(world, expectedUnloadDelay).thenAccept(future::complete),
                MainThreadExecutor.INSTANCE); // Merge this with the other method
            return future;
        }

        if (world == null) {
            return CompletableFuture.completedFuture(null);
        }

        /*
        if(!isTemporary(world)) {
            return CompletableFuture.completedFuture(null);
        }
         */

        world.setAutoSave(false);

        Bukkit.unloadWorld(world, false);
        File worldFolder = world.getWorldFolder();

        Runnable task = () -> {
            try {
                FileUtils.deleteDirectory(worldFolder);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };

        if (shutdown) {
            TimeUnit unit = TimeUnit.MILLISECONDS;
            long time = expectedUnloadDelay.as(unit);
            return CompletableFuture.runAsync(task, CompletableFuture.delayedExecutor(time, unit));
        }

        return CompletableFuture.runAsync(task);
    }

    public CompletableFuture<Void> deleteAllTemporaryWorlds() {
        File worldContainer = Bukkit.getWorldContainer();

        if (!worldContainer.exists() || !worldContainer.isDirectory()) {
            System.out.println("World container does not exist, or is not a directory");
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            File[] files = worldContainer.listFiles();

            if (files == null) {
                System.out.println("World container is empty");
                return;
            }

            for (File file : files) {
                if (!file.isDirectory()) {
                    System.out.println("Skipping " + file.getName() + " because it's not a directory");
                    continue;
                }

                if (!isTemporary(file.getName())) {
                    System.out.println("Skipping " + file.getName() + " because it's not temporary");
                    continue;
                }

                try {
                    FileUtils.deleteDirectory(file);
                    System.out.println("Deleted " + file.getName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
