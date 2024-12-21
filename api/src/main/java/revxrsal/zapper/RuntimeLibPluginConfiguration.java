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

    private final String libsFolder;
    private final String relocationPrefix;
    private final List<Dependency> dependencies;
    private final List<Repository> repositories;
    private final List<Relocation> relocations;

    RuntimeLibPluginConfiguration(String libsFolder, String relocationPrefix, List<Dependency> dependencies, List<Repository> repositories, List<Relocation> relocations) {
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
                    split[2]
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

    public String getLibsFolder() {
        return this.libsFolder;
    }

    public String getRelocationPrefix() {
        return this.relocationPrefix;
    }

    public List<Dependency> getDependencies() {
        return this.dependencies;
    }

    public List<Repository> getRepositories() {
        return this.repositories;
    }

    public List<Relocation> getRelocations() {
        return this.relocations;
    }

    public String toString() {
        return "RuntimeLibPluginConfiguration(libsFolder=" + this.getLibsFolder() + ", relocationPrefix=" + this.getRelocationPrefix() + ", dependencies=" + this.getDependencies() + ", repositories=" + this.getRepositories() + ", relocations=" + this.getRelocations() + ")";
    }
}
