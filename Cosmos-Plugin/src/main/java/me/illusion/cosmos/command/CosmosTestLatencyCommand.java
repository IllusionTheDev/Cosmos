package me.illusion.cosmos.command;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.utilities.command.command.impl.AdvancedCommand;
import me.illusion.cosmos.utilities.command.command.impl.ExecutionContext;
import me.illusion.cosmos.world.VoidGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;

public class CosmosTestLatencyCommand extends AdvancedCommand {

    private final CosmosPlugin plugin;

    public CosmosTestLatencyCommand(CosmosPlugin plugin) {
        super("cosmos test latency");

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, ExecutionContext context) {
        sender.sendMessage("Latency test!");

        // Let's create a world, set a block, force-save and measure the time it takes to do so.
        String worldName = "cosmos-test";

        long start = System.currentTimeMillis();
        World world = new WorldCreator(worldName).generator(new VoidGenerator()).createWorld();
        long end = System.currentTimeMillis();

        sender.sendMessage("World creation took " + (end - start) + "ms");

        new Location(world, 0, 0, 0).getBlock().setType(Material.STONE);

        FileLatencyTester fileLatencyTester = new FileLatencyTester(Path.of(world.getWorldFolder().getAbsolutePath(), "region"));

        fileLatencyTester.start();
        start = System.currentTimeMillis();
        Bukkit.unloadWorld(world, true);
        end = System.currentTimeMillis();

        sender.sendMessage("World save took " + (end - start) + "ms");

        Bukkit.getScheduler().runTaskLater(plugin, fileLatencyTester::cancel, 20 * 10);
    }

    private static class FileLatencyTester extends Thread {

        private final WatchService watchService;
        private final long start;
        private final AtomicBoolean running = new AtomicBoolean(true);

        public FileLatencyTester(Path path) {
            try {
                this.watchService = path.getFileSystem().newWatchService();
                path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            this.start = System.currentTimeMillis();

            setDaemon(true);
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    if (!running.get()) {
                        break;
                    }

                    watchService.take();
                    long end = System.currentTimeMillis();
                    System.out.println("File latency: " + (end - start) + "ms");
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        public void cancel() {
            running.set(false);
            try {
                watchService.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            interrupt();
        }
    }
}
