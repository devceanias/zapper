package revxrsal.zapper;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.zapper.classloader.URLClassLoaderWrapper;
import revxrsal.zapper.meta.MetaReader;

import java.io.File;
import java.net.URLClassLoader;

/**
 * An extension of {@link JavaPlugin} that downloads dependencies at runtime.
 * <p>
 * This should only be used in tandem with the Gradle plugin! Please consult
 * the documentation otherwise.
 */
public abstract class ZapperJavaPlugin extends JavaPlugin {

    static {
        MetaReader meta = MetaReader.create();
        RuntimeLibPluginConfiguration config = RuntimeLibPluginConfiguration.parse();
        File libraries = new File(meta.dataFolder(), config.getLibsFolder());
        if (!libraries.exists()) {
            // "ur plugin slow!!"
            Bukkit.getLogger().info("[" + meta.pluginName() + "] It appears you're running " + meta.pluginName() + " for the first time.");
            Bukkit.getLogger().info("[" + meta.pluginName() + "] Please give me a few seconds to install dependencies. This is a one-time process.");
        }
        DependencyManager dependencyManager = new DependencyManager(
                libraries,
                URLClassLoaderWrapper.wrap((URLClassLoader) ZapperJavaPlugin.class.getClassLoader())
        );
        config.getDependencies().forEach(dependencyManager::dependency);
        config.getRepositories().forEach(dependencyManager::repository);
        config.getRelocations().forEach(dependencyManager::relocate);
        dependencyManager.load();
    }
}