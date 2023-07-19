package me.illusion.cosmos.menu;

import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosContainerRegistry;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.serialization.CosmosSerializerRegistry;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import me.illusion.cosmos.utilities.hook.WorldEditUtils;
import me.illusion.cosmos.utilities.menu.base.BaseMenu;
import me.illusion.cosmos.utilities.menu.base.ConfigurableMenu;
import me.illusion.cosmos.utilities.menu.button.MultiSwitch;
import me.illusion.cosmos.utilities.menu.configuration.ConfigurationApplicator;
import me.illusion.cosmos.utilities.menu.layer.BaseLayer;
import me.illusion.cosmos.utilities.menu.registry.communication.UpdatableMenu;
import me.illusion.cosmos.utilities.menu.selection.Selection;
import me.illusion.cosmos.utilities.storage.MessagesFile;
import me.illusion.cosmos.utilities.text.Placeholder;
import me.illusion.cosmos.utilities.text.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TemplateCreationMenu implements UpdatableMenu {

    private final CosmosPlugin cosmos;
    private final MessagesFile messages;
    private final UUID viewerId;
    private final CosmosContainerRegistry containerRegistry;
    private final CosmosSerializerRegistry serializerRegistry;
    private final UpdatableMenu fallback;
    private ConfigurableMenu menu;
    private BaseLayer baseLayer;
    private String name;
    private CosmosDataContainer container;
    private CosmosSerializer serializer;
    private MultiSwitch<CosmosDataContainer> containerMultiSwitch;

    private boolean created;

    public TemplateCreationMenu(CosmosPlugin cosmos, UUID viewerId) {
        this(cosmos, viewerId, null);
    }

    public TemplateCreationMenu(CosmosPlugin cosmos, UUID viewerId, UpdatableMenu fallback) {
        this.cosmos = cosmos;
        this.messages = cosmos.getMessages();
        this.viewerId = viewerId;
        this.containerRegistry = cosmos.getContainerRegistry();
        this.serializerRegistry = cosmos.getSerializerRegistry();
        this.fallback = fallback;

        setup();

        cosmos.getMenuRegistry().getUpdatableMenuRegistry().register(this);
    }

    public void setup() {
        Player viewer = getViewer();

        ConfigurableMenu baseMenu = (ConfigurableMenu) cosmos.getMenuRegistry().create(getIdentifier(), viewer);
        this.menu = baseMenu;

        Cuboid selection = WorldEditUtils.getPlayerSelection(viewer);

        if (selection == null) {
            messages.sendMessage(viewer, "template.set-no-selection");
            openFallback();
            return;
        }

        name = "Unnamed";
        serializer = serializerRegistry.get("worldedit");

        ConfigurationApplicator applicator = baseMenu.getApplicator();
        baseLayer = new BaseLayer(baseMenu);
        menu.addRenderable(baseLayer);

        containerMultiSwitch = new MultiSwitch<>(applicator.createButton("c", (event) -> {
        }), new ArrayList<>(containerRegistry.getLoadedContainers()));
        containerMultiSwitch.setChoice(containerRegistry.getDefaultContainer());
        containerMultiSwitch.onChoiceUpdate(irrelevant -> {
            refresh();
        });

        Selection containerSelection = baseMenu.getMask().selection("c");
        baseLayer.applyRawSelection(containerSelection, containerMultiSwitch);

        applicator.registerButton(baseLayer, "n", () -> {
            messages.sendMessage(viewer, "menu.template.set-name");
            cosmos.getMenuRegistry().getHiddenMenuTracker().holdForInput(menu, (input) -> {
                this.name = input;
                refresh();
            });
        });

        applicator.registerButton(baseLayer, "create", () -> {
            if (name == null) {
                messages.sendMessage(viewer, "template.set-no-name");
                return;
            }

            if (container == null) {
                messages.sendMessage(viewer, "template.set-no-container");
                return;
            }

            created = true;
            serializer.createArea(selection, viewer.getLocation()).thenAccept(area -> {

                container.saveTemplate(name, area).thenRun(() -> {
                    messages.sendMessage(viewer, "template.created", new Placeholder<>("template", name));
                }).thenRun(this::openFallback);

                cosmos.getTemplateCache().register(name, area);
            });
        });

        applicator.registerButton(baseLayer, "close", this::openFallback);
        refresh();
        open();
    }

    @Override
    public void refresh() {

        container = containerMultiSwitch.getSelectedChoice();
        List<Placeholder<Player>> placeholderList = List.of(
                new Placeholder<>("TEMPLATE_NAME", name),
                new Placeholder<>("TEMPLATE_CONTAINER", TextUtils.capitalize(containerMultiSwitch.getSelectedChoice().getName())),
                new Placeholder<>("TEMPLATE_SERIALIZER", TextUtils.capitalize(serializer.getName())));

        menu.setItemPlaceholders(placeholderList); // Some items are stored on elements map in menu, some are in the layer
        baseLayer.setItemPlaceholders(placeholderList); // So we need to update both
        menu.forceUpdate();
    }

    private void openFallback() {
        this.close();

        if (fallback == null) {
            return;
        }

        if (!created) { // Doesn't create a new instance of the inventory if not changes were made
            fallback.open();
            return;
        }

        new TemplateViewMenu(cosmos, viewerId).open();
    }

    @Override
    public Player getViewer() {
        return Bukkit.getPlayer(viewerId);
    }

    @Override
    public BaseMenu getMenu() {
        return menu;
    }

    @Override
    public String getIdentifier() {
        return "template-creation";
    }
}
