package me.illusion.cosmos.session.task;

import java.time.Instant;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A task that will unload a session after a specified amount of time. This task is self-correcting, meaning that if the server is lagging, it will correct
 * itself by using recursion.
 *
 * @author Illusion
 */
public class UnloadTask implements Runnable {

    private final JavaPlugin plugin;
    private final UnloadRequest request;

    public UnloadTask(JavaPlugin plugin, UnloadRequest request) {
        this.plugin = plugin;
        this.request = request;

        run();
    }

    @Override
    public void run() {
        if(request.getFuture().isDone()) {
            // Already completed
            return;
        }

        // Self-correcting recursion
        long currentEpoch = Instant.now().getEpochSecond();
        long delay = request.getEpoch() - currentEpoch;

        long delayTicks = delay * 20;

        if (delayTicks < 5) {
            // 0.25 seconds is okay
            request.complete();
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, this, delayTicks / 2); // Re-run the task in half the remaining time, so we can self-correct
        }
    }

    /**
     * Cancels the unload task.
     */
    public void cancel() {
        if (!request.getFuture().isDone()) {
            request.cancel();
        }
    }
}
