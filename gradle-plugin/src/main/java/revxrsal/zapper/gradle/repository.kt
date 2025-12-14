package revxrsal.zapper.gradle

/**
 * A basic interface DSL for specifying repositories to be used
 * for dependencies
 */
interface RepositoryDsl {

    /**
     * Adds the given repository to the repositories list
     */
    fun maven(url: String)

    /**
     * Tells Zapper to include the project repositories for
     * resolving dependencies
     */
    fun includeProjectRepositories(include: Boolean = true)
}

/**
 * A basic implementation of [RepositoryDsl]
 */
internal class BasicRepositoryDsl : RepositoryDsl {

    /**
     * The repositories list
     */
    val repositories = mutableListOf<String>()

    /**
     * Should project repositories be included?
     */
    var includeProjectRepositories = true

    /**
     * Adds the given repository to the repositories list
     */
    override fun maven(url: String) {
        repositories.add(url)
    }

    /**
     * Tells Zapper to include the project repositories for
     * resolving dependencies
     */
    override fun includeProjectRepositories(include: Boolean) {
        includeProjectRepositories = include
    }
}

