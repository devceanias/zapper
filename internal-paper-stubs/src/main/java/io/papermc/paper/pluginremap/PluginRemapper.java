package io.papermc.paper.pluginremap;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class PluginRemapper {

    public PluginRemapper(final Path pluginsDir) {
        throw new RuntimeException("Stub!");
    }

    public static @Nullable PluginRemapper create(final Path pluginsDir) {
        throw new RuntimeException("Stub!");
    }

    public void shutdown() {
        throw new RuntimeException("Stub!");
    }

    public void save(final boolean clean) {
        throw new RuntimeException("Stub!");
    }

    // Called on startup and reload
    public void loadingPlugins() {
        throw new RuntimeException("Stub!");
    }

    // Called after all plugins enabled during startup/reload
    public void pluginsEnabled() {
        throw new RuntimeException("Stub!");
    }

    public List<Path> remapLibraries(final List<Path> libraries) {
        throw new RuntimeException("Stub!");
    }

    public Path rewritePlugin(final Path plugin) {
        throw new RuntimeException("Stub!");
    }

    public List<Path> rewriteExtraPlugins(final List<Path> plugins) {
        throw new RuntimeException("Stub!");
    }

    public List<Path> rewritePluginDirectory(final List<Path> jars) {
        throw new RuntimeException("Stub!");
    }

    private CompletableFuture<Path> remapPlugin(
            final RemappedPluginIndex index,
            final Path inputFile
    ) {
        return this.remap(index, inputFile, false);
    }

    private CompletableFuture<Path> remapLibrary(
            final RemappedPluginIndex index,
            final Path inputFile
    ) {
        return this.remap(index, inputFile, true);
    }

    /**
     * Returns the remapped file if remapping was necessary, otherwise null.
     *
     * @param index     remapped plugin index
     * @param inputFile input file
     * @return remapped file, or inputFile if no remapping was necessary
     */
    private CompletableFuture<Path> remap(
            final RemappedPluginIndex index,
            final Path inputFile,
            final boolean library
    ) {
        throw new RuntimeException("Stub!");
    }
}
