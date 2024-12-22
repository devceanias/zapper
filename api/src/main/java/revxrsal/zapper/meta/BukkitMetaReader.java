package revxrsal.zapper.meta;

import org.jetbrains.annotations.NotNull;
import revxrsal.zapper.util.ClassLoaderReader;

import java.io.File;

final class BukkitMetaReader implements MetaReader {

    @Override
    public @NotNull String pluginName() {
        return ClassLoaderReader.getDescription(BukkitMetaReader.class).getName();
    }

    @Override
    public @NotNull File dataFolder() {
        return ClassLoaderReader.getDataFolder(BukkitMetaReader.class);
    }
}
