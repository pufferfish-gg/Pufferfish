package gg.pufferfish.pufferfish.flare.collectors;

import co.technove.flare.live.CollectorData;
import co.technove.flare.live.LiveCollector;
import co.technove.flare.live.formatter.SuffixFormatter;
import gg.pufferfish.pufferfish.flare.CustomCategories;
import org.bukkit.Bukkit;
import org.bukkit.World;
import io.papermc.paper.util.MCUtil;

import java.time.Duration;

public class WorldCountCollector extends LiveCollector {

    private static final CollectorData PLAYER_COUNT = new CollectorData("airplane:world:playercount", "Player Count", "The number of players currently on the server.", new SuffixFormatter(" Player", " Players"), CustomCategories.ENTITIES_AND_CHUNKS);
    private static final CollectorData ENTITY_COUNT = new CollectorData("airplane:world:entitycount", "Entity Count", "The number of entities in all worlds", new SuffixFormatter(" Entity", " Entities"), CustomCategories.ENTITIES_AND_CHUNKS);
    private static final CollectorData CHUNK_COUNT = new CollectorData("airplane:world:chunkcount", "Chunk Count", "The number of chunks currently loaded.", new SuffixFormatter(" Chunk", " Chunks"), CustomCategories.ENTITIES_AND_CHUNKS);
    private static final CollectorData TILE_ENTITY_COUNT = new CollectorData("airplane:world:blockentitycount", "Block Entity Count", "The number of block entities currently loaded.", new SuffixFormatter(" Block Entity", " Block Entities"), CustomCategories.ENTITIES_AND_CHUNKS);

    public WorldCountCollector() {
        super(PLAYER_COUNT, ENTITY_COUNT, CHUNK_COUNT, TILE_ENTITY_COUNT);

        this.interval = Duration.ofSeconds(5);
    }

    @Override
    public void run() {
        MCUtil.ensureMain(() -> {
            int entities = 0;
            int chunkCount = 0;
            int tileEntityCount = 0;

            if (!Bukkit.isStopping()) {
                for (World world : Bukkit.getWorlds()) {
                    entities += world.getEntityCount();
                    chunkCount += world.getChunkCount();
                    tileEntityCount += world.getTileEntityCount();
                }
            }

            this.report(PLAYER_COUNT, Bukkit.getOnlinePlayers().size());
            this.report(ENTITY_COUNT, entities);
            this.report(CHUNK_COUNT, chunkCount);
            this.report(TILE_ENTITY_COUNT, tileEntityCount);
        });
    }
}
