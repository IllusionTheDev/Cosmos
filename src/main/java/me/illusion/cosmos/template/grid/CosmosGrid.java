package me.illusion.cosmos.template.grid;

import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;

public interface CosmosGrid {

    CompletableFuture<PastedArea> paste(TemplatedArea area);

    default CompletableFuture<PastedArea> paste(CompletableFuture<TemplatedArea> areaFuture) {
        return areaFuture.thenCompose(this::paste);
    }

}
