package me.illusion.cosmos.session;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Data;
import lombok.Getter;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.template.PastedArea;

@Getter
@Data
public class CosmosSession {

    private final UUID uuid;
    private final PastedArea pastedArea;

    public CompletableFuture<Void> unload() {
        return pastedArea.unload();
    }

    public CompletableFuture<Void> save(CosmosDataContainer container) {
        return container.saveTemplate(uuid.toString(), pastedArea);
    }

}
