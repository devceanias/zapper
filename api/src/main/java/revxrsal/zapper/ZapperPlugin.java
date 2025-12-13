package revxrsal.zapper;

import revxrsal.zapper.classloader.URLClassLoaderWrapper;
import revxrsal.zapper.meta.MetaReader;

import java.io.File;
import java.net.URLClassLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * An extension of {@link JavaPlugin} that downloads dependencies at runtime.
 * <p>
 * This should only be used in tandem with the Gradle plugin! Please consult
 * the documentation otherwise.
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class ZapperPlugin extends JavaPlugin {
    static {
        final MetaReader meta = MetaReader.create();
        final RuntimeLibPluginConfiguration config = RuntimeLibPluginConfiguration.parse();
        final File libraries = new File(meta.dataFolder(), config.getLibsFolder());
        final String name = meta.pluginName();

        final DependencyManager manager = new DependencyManager(
            libraries, URLClassLoaderWrapper.wrapLoader((URLClassLoader) ZapperPlugin.class.getClassLoader())
        );

        if (!libraries.exists()) {
            // "ur plugin slow!!"
            Bukkit.getLogger().info(
                "[" + name + "] It appears you're running " + name + " for the first time."
            );

            Bukkit.getLogger().info(
                "[" + name + "] Please give me a few seconds to install dependencies. This is a one-time process."
            );
        }

        config.getDependencies().forEach(manager::dependency);
        config.getRepositories().forEach(manager::repository);
        config.getRelocations().forEach(manager::relocate);

        manager.load();
    }
}