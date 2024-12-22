package io.papermc.paper.plugin.bootstrap;

import io.papermc.paper.plugin.configuration.PluginMeta;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;

/**
 * Represents the context provided to a {@link PluginBootstrap} during both the bootstrapping and plugin
 * instantiation logic.
 * A bootstrap context may be used to access data or logic usually provided to {@link org.bukkit.plugin.Plugin} instances
 * like the plugin's configuration or logger during the plugins bootstrap.
 */
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface PluginProviderContext {

    /**
     * Provides the plugin's configuration.
     *
     * @return the plugin's configuration
     */
    PluginMeta getConfiguration();

    /**
     * Provides the path to the data directory of the plugin.
     *
     * @return the previously described path
     */
    Path getDataDirectory();

    /**
     * Provides the path to the originating source of the plugin, such as the plugin's JAR file.
     *
     * @return the previously described path
     */
    Path getPluginSource();

}
