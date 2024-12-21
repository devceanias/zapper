package revxrsal.zapper.gradle

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.withType
import java.io.File

/**
 * The plugin version
 */
private const val PLUGIN_VERSION: String = "1.0.2"

/**
 * The Zapper Gradle plugin collects information about the zapped dependencies
 * and merges them into raw text files that are read by the Zapper API.
 */
class ZapperPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin("com.github.johnrengelman.shadow")) {
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
                project.createRepositoriesFile(outputDir, extension)

                if (extension.relocations.isNotEmpty()) {
                    project.createRelocationsFile(outputDir, extension)
                }

                createZappersFile(outputDir, zap)

                val configFile = outputDir.resolve("zapper.properties")
                configFile.writeText(extension.toPropertiesFile())
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
 * Generates the relocations.txt file
 */
private fun Project.createRelocationsFile(outputDir: File, extension: ZapperExtension) {
    val relocationsFile = outputDir.resolve("relocations.txt")
    project.plugins.withId("com.github.johnrengelman.shadow") {
        val relocationRules = mutableListOf<String>()
        project.tasks.withType<ShadowJar>().configureEach {
            extension.relocations.forEach {
                relocationRules.add("${it.pattern}:${extension.relocationPrefix}.${it.newPattern}")
                relocate(it.pattern, "${extension.relocationPrefix}.${it.newPattern}")
            }
            relocate("revxrsal.zapper", "${extension.relocationPrefix}.zapper")
        }
        relocationsFile.writeText(relocationRules.joinToString("\n"))
    }
}

/**
 * Generates the dependencies.txt file
 */
private fun createZappersFile(outputDir: File, runtimeLib: Configuration) {
    val runtimeLibsFile = outputDir.resolve("dependencies.txt")
    val runtimeLibDependencies = runtimeLib.resolvedConfiguration
        .resolvedArtifacts
        .joinToString("\n") { it.moduleVersion.id.toString() }

    runtimeLibsFile.writeText(runtimeLibDependencies)
}

/**
 * Generates the repositories.txt file
 */
private fun Project.createRepositoriesFile(outputDir: File, extension: ZapperExtension) {
    val repositoriesFile = outputDir.resolve("repositories.txt")
    val repositories = extension.repositries.toMutableSet()
    if (extension.includeProjectRepositories) {
        project.repositories.forEach {
            if (it is MavenArtifactRepository) {
                repositories.add(it.url.toString())
            }
        }
    }
    repositoriesFile.writeText(repositories.joinToString("\n"))
}

/**
 * Adds the Zapper API library
 */
private fun Project.addZapperDependencies() {
    dependencies.add("implementation", "io.github.revxrsal:zapper.api:${PLUGIN_VERSION}")
}
