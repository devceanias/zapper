package revxrsal.zapper.gradle

import org.gradle.api.Action

/**
 * The maven central repository, which is added by default.
 */
private const val MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2/"

/**
 * Configure Zapper properties
 */
open class ZapperExtension {

    /**
     * The subfolder in the plugin directory where libraries
     * should be installed
     */
    var libsFolder: String = "libraries"

    /**
     * The relocation prefix of all libraries
     */
    var relocationPrefix: String = "zapper"

    /**
     * The repositories URLs
     */
    private var _repositories = mutableListOf(MAVEN_CENTRAL)

    /**
     * The relocation rules
     */
    private var _relocations = mutableListOf<Relocation>()

    /**
     * The currently added repositories
     */
    val repositories: List<String> get() = _repositories

    /**
     * The current relocation rules
     */
    val relocations: List<Relocation> get() = _relocations

    /**
     * Should project repositories be remembered for downloading
     * repositories at runtime?
     */
    internal var includeProjectRepositories = true

    /**
     * Configures the repositories that are used for downloading dependencies.
     *
     * See [RepositoryDsl]
     */
    fun repositories(configure: Action<RepositoryDsl>) {
        val dsl = BasicRepositoryDsl()
        configure.execute(dsl)
        _repositories = dsl.repositories
        includeProjectRepositories = dsl.includeProjectRepositories
    }

    /**
     * Adds a relocation rule
     */
    fun relocate(pattern: String, newPattern: String) {
        _relocations.add(Relocation(pattern, newPattern))
    }

    /**
     * A fancy toString implementation
     */
    override fun toString(): String {
        return "RuntimeLibsExtension(libsFolder='$libsFolder', includeProjectRepositories=$includeProjectRepositories, repositries=$repositories)"
    }

    /**
     * Generates the content of the properties file of this extension
     */
    internal fun toPropertiesFile(): String {
        //language=Properties
        return """
            libs-folder=${libsFolder}
            relocation-prefix=${relocationPrefix}
        """.trimIndent()
    }
}
