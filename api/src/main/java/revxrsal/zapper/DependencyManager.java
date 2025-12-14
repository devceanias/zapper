/*
 * This file is part of Zapper, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.zapper;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.zapper.classloader.URLClassLoaderWrapper;
import revxrsal.zapper.meta.MetaReader;
import revxrsal.zapper.relocation.Relocation;
import revxrsal.zapper.relocation.Relocator;
import revxrsal.zapper.repository.Repository;

import java.io.File;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import java.util.logging.Logger;
import java.net.URL;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

public final class DependencyManager implements DependencyScope {

    public static boolean FAILED_TO_DOWNLOAD = false;
    private static final Pattern COLON = Pattern.compile(":");

    private final File directory;
    private final URLClassLoaderWrapper classLoader;

    private final List<Dependency> dependencies = new ArrayList<>();
    private final Set<Repository> repositories = new LinkedHashSet<>();
    private final List<Relocation> relocations = new ArrayList<>();
    private final MetaReader meta = MetaReader.create();

    public DependencyManager(@NotNull final File directory, @NotNull final URLClassLoaderWrapper classLoader) {
        this.directory = directory;
        this.classLoader = classLoader;
        this.repositories.add(Repository.mavenCentral());
    }

    @SneakyThrows
    public void load() {
        final Logger logger = Bukkit.getLogger();
        final String prefix = "[" + meta.pluginName() + "] ";

        try {
            final List<Path> paths = new ArrayList<>();
            for (final Dependency dep : dependencies) {
                logger.info(prefix + "Resolving dependency " + dep + ".");

                final File file = new File(directory, String.format("%s.%s-%s.jar", dep.getGroupId(), dep.getArtifactId(), dep.getVersion()));
                final File relocated = new File(directory, String.format("%s.%s-%s-relocated.jar", dep.getGroupId(),
                        dep.getArtifactId(), dep.getVersion()));

                if (hasRelocations() && relocated.exists()) {
                    logger.info(
                        prefix +
                        "Using existing relocated jar for " +
                        dep + ": " +
                        relocated.getName() +
                        " (" +
                        relocated.length() +
                        " bytes)."
                    );

                    paths.add(relocated.toPath());

                    continue;
                }

                if (!file.exists()) {
                    boolean succeeded = false;
                    List<String> failedRepos = null;
                    for (final Repository repository : repositories) {
                        logger.info(
                            prefix + "Attempting download of " + dep + " from repository " + repository + "."
                        );

                        final DependencyDownloadResult result = dep.download(file, repository);
                        if (result.wasSuccessful()) {
                            logger.info(prefix + "Downloaded " + dep + " (" + file.length() + " bytes) from " + repository + ".");
                            succeeded = true;
                            break;
                        } else
                            (failedRepos == null ? failedRepos = new ArrayList<>() : failedRepos).add(repository.toString());

                        final String failedPrefix = prefix + "Failed downloading " + dep + " from " + repository + ": ";

                        if (result instanceof final DependencyDownloadResult.Failure failure) {
                            logger.warning(failedPrefix + failure.getError());
                        } else {
                            logger.warning(failedPrefix + "unknown error.");
                        }
                    }

                    if (failedRepos != null && !succeeded) {
                        throw new DependencyDownloadException(
                            dep,
                            "Could not find dependency in any of the following repositories: " + String.join("\n", failedRepos)
                        );
                    }
                } else {
                    logger.info(
                        prefix +
                        "Using cached jar for " +
                        dep +
                        ": " +
                        file.getName() +
                        " (" +
                        file.length() +
                        " bytes)."
                    );
                }
                if (hasRelocations() && !relocated.exists()) {
                    Relocator.relocate(file, relocated, relocations);
                    logger.info(
                        prefix +
                        "Relocated " +
                        dep +
                        " to " +
                        relocated.getName() +
                        " (" +
                        relocated.length() +
                        " bytes)."
                    );

                    file.delete(); // no longer need the original dependency
                }
                if (hasRelocations())
                    paths.add(relocated.toPath());
                else
                    paths.add(file.toPath());
            }
            for (final Path path : paths) {
                final URL url = path.toUri().toURL();

                if (!addToPaperLibraryLoader(url, logger, prefix)) {
                    classLoader.addURL(url);
                    logger.info(prefix + "Added to plugin classloader: " + path.getFileName() + ".");
                }
            }

            // Default TCCL to the plugin classloader so libraries relying on it can see classes correctly.
            Thread.currentThread().setContextClassLoader(ZapperPlugin.class.getClassLoader());
        } catch (final DependencyDownloadException exception) {
            if (exception.getCause() instanceof UnknownHostException) {
                logger.info(
                    prefix +
                    "It appears you do not have an internet connection. Please provide an internet connection for once at least."
                );

                FAILED_TO_DOWNLOAD = true;
            } else throw exception;
        } finally {
            logger.info(prefix + "Dependency resolution finished. Total dependencies: " + dependencies.size() + ".");
        }
    }

    /**
     * Attempts to add a URL to Paper's library loader (similar to Libby).
     */
    private boolean addToPaperLibraryLoader(
        final @NotNull URL url, final @NotNull Logger logger, final @NotNull String prefix
    ) {
        try {
            final Class<?> paperLoader = Class.forName(
                "io.papermc.paper.plugin.entrypoint.classloader.PaperPluginClassLoader"
            );

            final ClassLoader pluginLoader = ZapperPlugin.class.getClassLoader();

            if (!paperLoader.isAssignableFrom(pluginLoader.getClass())) {
                return false;
            }

            final Field loaderField = paperLoader.getDeclaredField("libraryLoader");

            loaderField.setAccessible(true);

            final Object libraryLoader = loaderField.get(pluginLoader);

            if (!(libraryLoader instanceof final URLClassLoader urlLoader)) {
                return false;
            }

            final Method addMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);

            addMethod.setAccessible(true);
            addMethod.invoke(urlLoader, url);

            logger.info(prefix + "Added to Paper library loader: " + url.getPath() + ".");

            return true;
        } catch (final ClassNotFoundException ignored) {
            return false;
        } catch (final Throwable throwable) {
            logger.warning(prefix + "Error adding URL to Paper library loader: " + throwable.getMessage() + "!");

            return false;
        }
    }

    @Override
    public void dependency(@NotNull final Dependency dependency) {
        dependencies.add(dependency);
    }

    public void dependency(@NotNull final String dependency) {
        final String[] parts = COLON.split(dependency);
        dependencies.add(new Dependency(parts[0], parts[1], parts[2], parts.length == 4 ? parts[3] : null));
    }

    public void dependency(@NotNull final String groupId, @NotNull final String artifactId, @NotNull final String version) {
        dependencies.add(new Dependency(groupId, artifactId, version));
    }

    public void dependency(@NotNull final String groupId, @NotNull final String artifactId, @NotNull final String version, @Nullable final String classifier) {
        dependencies.add(new Dependency(groupId, artifactId, version, classifier));
    }

    public void relocate(@NotNull final Relocation relocation) {
        relocations.add(relocation);
    }

    public void repository(@NotNull final Repository repository) {
        repositories.add(repository);
    }

    public boolean hasRelocations() {
        return !relocations.isEmpty();
    }

}
