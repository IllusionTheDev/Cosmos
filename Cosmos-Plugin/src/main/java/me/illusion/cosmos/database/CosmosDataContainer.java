package me.illusion.cosmos.database;

import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.template.TemplatedArea;

/**
 * A cosmos data container is a repository for storing and retrieving areas.
 * <p>
 *
 * @author Illusion
 */
public interface CosmosDataContainer {

    /**
     * Fetches a template from the container.
     *
     * @param name The name of the template
     * @return A future which will be completed with the template, or null if it does not exist
     */
    CompletableFuture<TemplatedArea> fetchTemplate(String name);

    /**
     * Saves a template to the container.
     *
     * @param name The name of the template
     * @param area The area to save
     * @return A future which will be completed when the save is done
     */
    CompletableFuture<Void> saveTemplate(String name, TemplatedArea area);

    /**
     * Deletes a template from the container.
     *
     * @param name The name of the template
     * @return A future which will be completed when the delete is done
     */
    CompletableFuture<Void> deleteTemplate(String name);

    /**
     * Flushes the container, saving all pending changes. This method is called automatically when the plugin is disabled, and is expected to be joined on.
     *
     * @return A future which will be completed when the flush is done
     */
    CompletableFuture<Void> flush();

    /**
     * Obtains the name of the container.
     *
     * @return The name of the container
     */
    String getName();

}
