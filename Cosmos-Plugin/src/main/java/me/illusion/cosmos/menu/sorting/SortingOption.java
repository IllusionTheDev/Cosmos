package me.illusion.cosmos.menu.sorting;

import java.util.Comparator;
import me.illusion.cosmos.menu.data.TemplateData;

public enum SortingOption {
    TEMPLATE_NAME((one, two) -> one.getTemplateName().compareToIgnoreCase(two.getTemplateName())),
    SERIALIZER_NAME((one, two) -> one.getSerializerName().compareToIgnoreCase(two.getSerializerName())),
    CONTAINER_NAME((one, two) -> one.getContainerName().compareToIgnoreCase(two.getContainerName()));

    private final Comparator<TemplateData> comparator;

    SortingOption(Comparator<TemplateData> comparator) {
        this.comparator = comparator;
    }

    public Comparator<TemplateData> getComparator() {
        return comparator;
    }
}
