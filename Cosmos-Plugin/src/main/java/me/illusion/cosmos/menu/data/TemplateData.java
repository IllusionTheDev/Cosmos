package me.illusion.cosmos.menu.data;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * This is a basic data class that represents the display data of a TemplatedArea. This does not contain the actual data of the template, as that would be too
 * RAM intensive.
 */
@Data
@AllArgsConstructor
public class TemplateData {

    private final String templateName;
    private final String serializerName;
    private final String containerName;

}
