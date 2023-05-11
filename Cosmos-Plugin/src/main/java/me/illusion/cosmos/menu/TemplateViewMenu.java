package me.illusion.cosmos.menu;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.menu.data.TemplateData;
import me.illusion.cosmos.menu.sorting.SortingOption;
import me.illusion.cosmos.utilities.menu.base.BaseMenu;
import me.illusion.cosmos.utilities.menu.base.ConfigurableMenu;
import me.illusion.cosmos.utilities.menu.button.Button;
import me.illusion.cosmos.utilities.menu.button.MultiSwitch;
import me.illusion.cosmos.utilities.menu.configuration.ConfigurationApplicator;
import me.illusion.cosmos.utilities.menu.layer.BaseLayer;
import me.illusion.cosmos.utilities.menu.layer.PaginableLayer;
import me.illusion.cosmos.utilities.menu.pagination.PaginableArea;
import me.illusion.cosmos.utilities.menu.registry.communication.UpdatableMenu;
import me.illusion.cosmos.utilities.text.Placeholder;
import me.illusion.cosmos.utilities.text.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TemplateViewMenu implements UpdatableMenu {

    private final CosmosPlugin cosmos;
    private final UUID viewerId;
    private final Set<TemplateData> templates = Sets.newConcurrentHashSet();
    private ConfigurableMenu menu;
    private PaginableArea area;
    private MultiSwitch<SortingOption> sortingOptionSwitch;

    public TemplateViewMenu(CosmosPlugin cosmos, UUID viewerId) {
        this.cosmos = cosmos;
        this.viewerId = viewerId;

        setup();

        cosmos.getMenuRegistry().getUpdatableMenuRegistry().register(this);
    }

    private void setup() {
        Player viewer = getViewer();

        ConfigurableMenu baseMenu = (ConfigurableMenu) cosmos.getMenuRegistry().create("template-view", viewer);
        this.menu = baseMenu;

        ConfigurationApplicator applicator = baseMenu.getApplicator();

        BaseLayer baseLayer = new BaseLayer(baseMenu);
        PaginableLayer paginableLayer = new PaginableLayer(baseMenu);

        area = new PaginableArea(baseMenu.getMask().selection("."));
        paginableLayer.addArea(area);

        applicator.registerButton(baseLayer, "create", () -> {
            // TODO: write this
        });

        applicator.registerButton(baseLayer, "left", paginableLayer::previousPage);
        applicator.registerButton(baseLayer, "right", paginableLayer::nextPage);
        applicator.registerButton(baseLayer, "close", this::close);

        sortingOptionSwitch = new MultiSwitch<>(applicator.createButton("sort", (event) -> {
        }), SortingOption.values());
        sortingOptionSwitch.setChoice(SortingOption.TEMPLATE_NAME);

        sortingOptionSwitch.onChoiceUpdate(irrelevant -> refresh());

        for (CosmosDataContainer container : cosmos.getContainerRegistry().getContainersAsCollection()) {
            if (!cosmos.getContainerRegistry().isEnabled(container.getName())) {
                continue; // Let's not crash things, shall we?
            }

            container.fetchAllTemplates().thenAccept(templateIds -> {
                for (String templateId : templateIds) {
                    container.fetchTemplateSerializer(templateId).thenAccept(serializer -> {
                        if (serializer == null) {
                            return;
                        }

                        TemplateData data = new TemplateData(templateId, serializer, container.getName());
                        templates.add(data);
                        refresh();
                    });
                }
            });
        }

        refresh();

        baseMenu.addRenderable(baseLayer, paginableLayer);
        baseMenu.open();
    }

    @Override
    public Player getViewer() {
        return Bukkit.getPlayer(viewerId);
    }

    @Override
    public void refresh() {
        area.clearArea();

        List<TemplateData> sorted = new ArrayList<>(templates);
        sorted.sort(sortingOptionSwitch.getSelectedChoice().getComparator());

        for (TemplateData data : sorted) {
            Button button = menu.getApplicator().createButton("active-item", (event) -> {
                // Left click to paste at the location, right click to delete
                CosmosDataContainer container = cosmos.getContainerRegistry().getContainer(data.getContainerName());
                if (event.isRightClick()) {
                    container.deleteTemplate(data.getTemplateName()).thenRun(this::refresh); // TODO: delegate this through the confirmation menu
                    return;
                }

                container.fetchTemplate(data.getTemplateName()).thenAccept(template -> {
                    if (template == null) {
                        return;
                    }

                    template.paste(getViewer().getLocation());
                });
            });

            button.setPlaceholders(
                new Placeholder<>("template-name", TextUtils.capitalize(data.getTemplateName())),
                new Placeholder<>("template-serializer", TextUtils.capitalize(data.getSerializerName())),
                new Placeholder<>("template-container", TextUtils.capitalize(data.getContainerName()))
            );

            area.addElement(button);
        }

        menu.forceUpdate();
    }

    @Override
    public BaseMenu getMenu() {
        return menu;
    }

    @Override
    public String getIdentifier() {
        return "cosmos-template-view-menu";
    }
}
