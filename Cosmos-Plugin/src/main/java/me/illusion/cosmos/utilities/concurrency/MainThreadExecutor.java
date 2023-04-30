package me.illusion.cosmos.utilities.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class MainThreadExecutor implements Executor {

    public static final MainThreadExecutor INSTANCE = new MainThreadExecutor();
    private static JavaPlugin plugin;


    private MainThreadExecutor() {
    }

    public static void init(JavaPlugin main) {
        MainThreadExecutor.plugin = main;
    }

    @Override
    public void execute(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Runnable task = () -> {
            runnable.run();
            latch.countDown();
        };

        Bukkit.getScheduler().runTask(plugin, task);

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace(); // if this happens, something is very very very very wrong
        }
    }
}
