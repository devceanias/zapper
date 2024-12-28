package revxrsal.zapper.repository;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import revxrsal.zapper.Dependency;

import java.io.File;
import java.net.URL;

/**
 * Represents a repository that can resolve {@link revxrsal.zapper.Dependency dependencies}
 */
public interface Repository {

    /**
     * Resolves the dependency URL that is downloaded.
     *
     * @param dependency Dependency to resolve
     * @return The URL to resolve
     * @throws Exception any exception that indicates that this repository could not
     *                   resolve the dependency
     */
    @NotNull URL resolve(@NotNull Dependency dependency) throws Exception;

    /**
     * Resolves the pom URL of dependency that is downloaded.
     *
     * @param dependency Dependency to resolve
     * @return The URL to resolve
     * @throws Exception any exception that indicates that this repository could not
     *                   resolve the dependency
     */
    @NotNull URL resolvePom(@NotNull Dependency dependency) throws Exception;

    /**
     * Creates a Maven repository from the specified URL.
     *
     * @param url the URL of the repository
     * @return the configured Maven repository
     */
    static @NotNull Repository maven(@NotNull URL url) {
        return MavenRepository.maven(url.toString());
    }

    /**
     * Creates a Maven repository from the specified URL string.
     *
     * @param url the URL of the repository as a string
     * @return the configured Maven repository
     */
    static @NotNull Repository maven(@NotNull String url) {
        return MavenRepository.maven(url);
    }

    /**
     * Returns a repository representing the local Maven directory (default: ~/.m2).
     *
     * @return the local Maven repository
     */
    @SneakyThrows
    static @NotNull Repository mavenLocal() {
        String userHome = System.getProperty("user.home");
        File repository = new File(userHome, ".m2" + File.separator + "repository");
        return maven(repository);
    }

    /**
     * Returns a repository representing a custom local Maven directory.
     *
     * @param directory the local Maven directory
     * @return the local Maven repository
     */
    @SneakyThrows
    static @NotNull Repository maven(@NotNull File directory) {
        return maven(directory.toURI().toURL().toString());
    }

    /**
     * Returns the Maven Central repository.
     *
     * @return the Maven Central repository
     */
    static @NotNull Repository mavenCentral() {
        return MavenRepository.mavenCentral();
    }

    /**
     * Returns the JitPack repository.
     *
     * @return the JitPack repository
     */
    static @NotNull Repository jitpack() {
        return MavenRepository.jitpack();
    }

    /**
     * Returns the PaperMC repository.
     *
     * @return the PaperMC repository
     */
    static @NotNull Repository paper() {
        return MavenRepository.paper();
    }

    /**
     * Returns the Minecraft repository.
     *
     * @return the Minecraft repository
     */
    static @NotNull Repository minecraft() {
        return MavenRepository.minecraft();
    }
}
