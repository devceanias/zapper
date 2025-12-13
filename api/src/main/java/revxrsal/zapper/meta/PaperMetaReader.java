package revxrsal.zapper.meta;

import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import org.jetbrains.annotations.NotNull;

final class PaperMetaReader implements MetaReader {

    @Override
    public @NotNull String pluginName() {
        ConfiguredPluginClassLoader classLoader = (ConfiguredPluginClassLoader) getClass().getClassLoader();
        return classLoader.getConfiguration().getName();
    }
}