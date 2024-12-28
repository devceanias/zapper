package revxrsal.zapper.transitive;

import org.jetbrains.annotations.NotNull;

/**
 * Enum representing the different Maven dependency scopes.
 * The Maven scopes define the classpath in which a dependency is used.
 */
public enum MavenScope {

    /**
     * The compile scope is used for dependencies that are required for compiling the project.
     */
    COMPILE("compile"),

    /**
     * The provided scope is used for dependencies that are required for compiling the project
     * but are expected to be provided by the runtime environment
     */
    PROVIDED("provided"),

    /**
     * The runtime scope is used for dependencies that are not needed for compiling the project
     * but are required at runtime.
     */
    RUNTIME("runtime"),

    /**
     * The test scope is used for dependencies that are only needed for testing and are not included
     * in the runtime or compile classpath.
     */
    TEST("test"),

    /**
     * The system scope is used for dependencies that are provided by the system (e.g., a local system dependency)
     * and must be explicitly declared in the POM.
     */
    SYSTEM("system"),

    /**
     * The import scope is used when importing dependencies from a BOM (Bill of Materials) for managing versions.
     */
    IMPORT("import");

    private final String scope;

    MavenScope(String scope) {
        this.scope = scope;
    }


    /**
     * Returns the scope as a string.
     *
     * @return The scope string.
     */
    public String getScope() {
        return scope;
    }

    /**
     * Returns the string representation of the scope.
     *
     * @return The scope string.
     */
    @Override
    public String toString() {
        return scope;
    }

    /**
     * Converts a string to its corresponding MavenScope enum value.
     *
     * @param scope The string representation of a Maven scope.
     * @return The corresponding MavenScope enum value.
     * @throws IllegalArgumentException If the provided string does not match any known MavenScope value.
     */
    public static @NotNull MavenScope fromString(@NotNull String scope) {
        for (MavenScope s : MavenScope.values()) {
            if (s.scope.equalsIgnoreCase(scope)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown scope: " + scope);
    }
}