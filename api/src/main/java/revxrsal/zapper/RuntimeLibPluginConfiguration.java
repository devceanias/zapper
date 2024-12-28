package revxrsal.zapper;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import revxrsal.zapper.relocation.Relocation;
import revxrsal.zapper.repository.Repository;
import revxrsal.zapper.util.ClassLoaderReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public final class RuntimeLibPluginConfiguration {

    private final @NotNull String libsFolder;
    private final @NotNull String relocationPrefix;
    private final @NotNull List<Dependency> dependencies;
    private final @NotNull List<Repository> repositories;
    private final @NotNull List<Relocation> relocations;

    RuntimeLibPluginConfiguration(
            @NotNull String libsFolder,
            @NotNull String relocationPrefix,
            @NotNull List<Dependency> dependencies,
            @NotNull List<Repository> repositories,
            @NotNull List<Relocation> relocations
    ) {
        this.libsFolder = libsFolder;
        this.relocationPrefix = relocationPrefix;
        this.dependencies = dependencies;
        this.repositories = repositories;
        this.relocations = relocations;
    }

    public static @NotNull RuntimeLibPluginConfiguration parse() {
        try {
            Properties config = parseProperties();
            String libsFolder = config.getProperty("libs-folder");
            String relocationPrefix = config.getProperty("relocation-prefix");
            List<Repository> repositories = parseRepositories();
            List<Dependency> dependencies = parseDependencies();
            List<Relocation> relocations = parseRelocations();
            return new RuntimeLibPluginConfiguration(
                    libsFolder,
                    relocationPrefix,
                    dependencies,
                    repositories,
                    relocations
            );
        } catch (IOException e) {
            throw new IllegalArgumentException("Generated Zapper files are missing. Have you applied the Gradle plugin?");
        }
    }

    private static @NotNull List<Relocation> parseRelocations() throws IOException {
        InputStream stream = ClassLoaderReader.getResource("zapper/relocations.txt");
        if (stream == null)
            return Collections.emptyList();
        List<Relocation> relocations = new ArrayList<>();
        for (String line : readAllLines(stream)) {
            String[] split = line.split(":");
            relocations.add(new Relocation(split[0], split[1]));
        }
        return relocations;
    }

    private static @NotNull List<Dependency> parseDependencies() {
        InputStream stream = ClassLoaderReader.getResource("zapper/dependencies.txt");
        if (stream == null)
            return Collections.emptyList();
        List<Dependency> dependencies = new ArrayList<>();
        for (String line : readAllLines(stream)) {
            String[] split = line.split(":");
            dependencies.add(new Dependency(
                    split[0],
                    split[1],
                    split[2],
                    split.length == 4 ? split[3] : null
            ));
        }
        return dependencies;
    }

    private static @NotNull List<Repository> parseRepositories() {
        InputStream stream = ClassLoaderReader.getResource("zapper/repositories.txt");
        if (stream == null)
            return Collections.emptyList();
        List<Repository> repos = new ArrayList<>();
        for (String line : readAllLines(stream)) {
            repos.add(Repository.maven(line));
        }
        return repos;
    }

    private static @SneakyThrows @NotNull Properties parseProperties() {
        Properties properties = new Properties();
        try (InputStream stream = ClassLoaderReader.getResource("zapper/zapper.properties")) {
            properties.load(stream);
        }
        return properties;
    }

    private static @SneakyThrows @NotNull List<String> readAllLines(@NotNull InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    public @NotNull String getLibsFolder() {
        return this.libsFolder;
    }

    public @NotNull String getRelocationPrefix() {
        return this.relocationPrefix;
    }

    public @NotNull List<Dependency> getDependencies() {
        return this.dependencies;
    }

    public @NotNull List<Repository> getRepositories() {
        return this.repositories;
    }

    public @NotNull List<Relocation> getRelocations() {
        return this.relocations;
    }

    public String toString() {
        return "RuntimeLibPluginConfiguration(libsFolder=" + this.getLibsFolder() + ", relocationPrefix=" + this.getRelocationPrefix() + ", dependencies=" + this.getDependencies() + ", repositories=" + this.getRepositories() + ", relocations=" + this.getRelocations() + ")";
    }
}
