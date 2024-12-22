package revxrsal.zapper.meta;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * A utility for parsing plugin metas. This integrates with Paper's paper-plugin.yml
 * format
 */
public interface MetaReader {

    /**
     * Returns the plugin name
     *
     * @return The plugin name
     */
    @NotNull String pluginName();

    /**
     * Returns the data folder of this plugin
     *
     * @return The data folder
     */
    default @NotNull File dataFolder() {
        return new File(Bukkit.getUpdateFolderFile().getParentFile() + File.separator + pluginName());
    }

    /**
     * Creates a new {@link MetaReader}
     *
     * @return A new meta reader
     */
    @SneakyThrows
    static @NotNull MetaReader create() {
        String classLoaderName = MetaReader.class.getClassLoader().getClass().getSimpleName();
        if (classLoaderName.contains("Paper"))
            return Class.forName("revxrsal.zapper.meta.PaperMetaReader")
                    .asSubclass(MetaReader.class)
                    .newInstance();
        return new BukkitMetaReader();
    }
}
