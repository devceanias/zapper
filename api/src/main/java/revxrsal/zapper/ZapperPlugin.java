package revxrsal.zapper;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import org.jetbrains.annotations.NotNull;
import revxrsal.zapper.classloader.URLClassLoaderWrapper;

import java.io.File;
import java.net.URLClassLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

/**
 * This should only be used in tandem with the Gradle plugin! Please consult
 * the documentation otherwise.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ZapperPlugin {
    public static void initialise(final @NotNull BootstrapContext context) {
        final RuntimeLibPluginConfiguration config = RuntimeLibPluginConfiguration.parse();
        final File libraries = new File(context.getDataDirectory().toFile(), config.getLibsFolder());

        final DependencyManager manager = new DependencyManager(
            libraries, URLClassLoaderWrapper.wrapLoader((URLClassLoader) context.getClass().getClassLoader())
        );

        final ComponentLogger logger = context.getLogger();

        if (!libraries.exists()) {
            // "ur plugin slow!!"
            logger.info(
                "It appears you're running {} for the first time.", context.getPluginMeta().getDisplayName()
            );

            logger.info("Please give me a few seconds to install dependencies. This is a one-time process.");
        }

        config.getDependencies().forEach(manager::dependency);
        config.getRepositories().forEach(manager::repository);
        config.getRelocations().forEach(manager::relocate);

        manager.load(logger);
    }
}