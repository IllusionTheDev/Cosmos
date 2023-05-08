package me.illusion.cosmos.pool.world;

import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import me.illusion.cosmos.utilities.time.Time;
import me.illusion.cosmos.world.VoidGenerator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.Vector;

@Builder
@Getter
public class WorldPoolSettings {

    @Default
    private int maxCachedWorlds = 5; // We'll keep this many worlds in the UNUSED state before unloading them

    @Default
    private int maxUnloadedWorlds = 25; // We'll delete worlds after this

    @Default
    private int preGeneratedWorlds = 2; // We'll pre-generate this many worlds

    @Default
    private ChunkGenerator chunkGenerator = new VoidGenerator();

    @Default
    private Vector spawnLocation = new Vector(0, 128, 0);

    @Default
    private int batchDelayTicks = 50; // When creating multiple worlds, we'll leave *this* many ticks between each world creation

    @Default
    private Time deletionDelay = new Time(1, TimeUnit.SECONDS); // We'll wait this long between unloading and deleting worlds

}
