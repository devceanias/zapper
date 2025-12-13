package revxrsal.zapper.gradle

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.withType
import java.io.File
import java.util.jar.JarFile
import org.gradle.kotlin.dsl.maven

/**
 * The plugin version
 */
private const val PLUGIN_VERSION: String = "1.3.4"

/**
 * The Zapper Gradle plugin collects information about the zapped dependencies
 * and merges them into raw text files that are read by the Zapper API.
 */
class ZapperPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin("com.gradleup.shadow")) {
            error("ShadowJar is required by the Zapper Gradle plugin. Please add ShadowJar v8.11.0")
        }

        project.extensions.create("zapper", ZapperExtension::class.java)

        // creates the 'zap' configuration
        val zap = project.configurations.create("zap") {
            isCanBeResolved = true
            isCanBeConsumed = false
            description = "Marks a dependency for downloading at runtime"
        }

        // include zapped dependencies as compileOnly
        project.afterEvaluate {
            configurations.getByName("compileOnly").extendsFrom(zap)
        }

        val outputDir = project.layout.buildDirectory.asFile.get().resolve("zapper")
        project.tasks.register("generateZapperFiles") {
            group = "build"
            description = "Generates information about dependencies to install and relocate at runtime"
            doLast {
                outputDir.mkdirs()

                val extension = project.zapper

                outputDir
                    .resolve("repositories.txt")
                    .writeLines(project.collectAllRepositories(extension))

                outputDir
                    .resolve("relocations.txt")
                    .writeLines(project.collectAllRelocations(extension))

                outputDir
                    .resolve("dependencies.txt")
                    .writeLines(project.collectAllDependencies(zap))

                outputDir.resolve("zapper.properties").writeText(extension.toPropertiesFile())
            }
        }

        project.addZapperDependencies()

        project.tasks.withType(Jar::class.java).configureEach {
            dependsOn("generateZapperFiles")

            from(outputDir) {
                include("dependencies.txt")
                include("relocations.txt")
                include("repositories.txt")
                include("zapper.properties")
                into("zapper")
            }
        }
    }
}

/**
 * Adds the Zapper API library
 */
private fun Project.addZapperDependencies() {
    repositories.maven("jitpack.io")

    dependencies.add(
        "implementation", "com.github.devceanias:zapper:${PLUGIN_VERSION}"
    )
}

private fun Project.collectAllDependencies(runtimeLib: Configuration): List<String> {
    val dependencies = LinkedHashSet<String>()

    dependencies.addAll(
        runtimeLib.resolvedConfiguration.resolvedArtifacts.map { artifact -> artifact.moduleVersion.id.toString() }
    )

    dependencies.addAll(collectZapperResource("dependencies.txt"))

    return dependencies.toList()
}

private fun Project.collectAllRelocations(extension: ZapperExtension): List<String> {
    val relocations = LinkedHashSet<String>()

    plugins.withId("com.gradleup.shadow") {
        tasks.withType<ShadowJar>().configureEach {
            extension.relocations.forEach {
                relocations.add("${it.pattern}:${extension.relocationPrefix}.${it.newPattern}")

                relocate(it.pattern, "${extension.relocationPrefix}.${it.newPattern}")
            }

            relocate("revxrsal.zapper", "${extension.relocationPrefix}.zapper")
        }
    }

    relocations.addAll(collectZapperResource("relocations.txt"))

    return relocations.toList()
}

private fun Project.collectAllRepositories(extension: ZapperExtension): List<String> {
    val repositories = LinkedHashSet<String>()

    repositories.addAll(extension.repositories)

    if (extension.includeProjectRepositories) {
        project.repositories.forEach { repository ->
            if (repository is MavenArtifactRepository) {
                repositories.add(repository.url.toString())
            }
        }
    }

    repositories.addAll(collectZapperResource("repositories.txt"))

    return repositories.toList()
}

private fun Project.collectZapperResource(fileName: String): List<String> {
    val configurations = listOfNotNull(
        configurations.findByName("compileClasspath"),
        configurations.findByName("runtimeClasspath")
    )

    val collected = LinkedHashSet<String>()

    configurations.forEach { configuration ->
        configuration.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
            val file = artifact.file

            if (!file.isFile || file.extension != "jar") {
                return@forEach
            }

            JarFile(file).use { jar ->
                val entry = jar.getJarEntry("zapper/$fileName") ?: return@use

                jar.getInputStream(entry).bufferedReader().useLines { lines ->
                    lines.filter { line -> line.isNotBlank() }.forEach { line -> collected.add(line) }
                }
            }
        }
    }

    return collected.toList()
}

private fun File.writeLines(lines: List<String>) {
    writeText(lines.joinToString("\n"))
}