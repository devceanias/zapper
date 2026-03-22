package revxrsal.zapper.gradle

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.util.jar.JarFile
import revxrsal.zapper.gradle.task.ZapperFilesTask

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

        val outputDirFile = project.layout.buildDirectory.asFile.get().resolve("zapper")

        configureShadowRelocations(project)

        val generateZapperFiles = project.tasks.register<ZapperFilesTask>("generateZapperFiles") {
            group = "build"
            description = "Generates information about dependencies to install and relocate at runtime"

            outputDirectory.set(project.layout.buildDirectory.dir("zapper"))

            zapDependencies.set(
                project.provider {
                    zap.resolvedConfiguration.resolvedArtifacts
                        .map { artifact -> artifact.moduleVersion.id.toString() }
                        .distinct()
                        .sorted()
                }
            )

            resourceClasspath.from(
                project.provider {
                    listOfNotNull(
                        project.configurations.findByName("compileClasspath"),
                        project.configurations.findByName("runtimeClasspath")
                    )
                }
            )

            projectRepositories.set(
                project.provider {
                    project.repositories
                        .filterIsInstance<MavenArtifactRepository>()
                        .map { repository -> repository.url.toString() }
                        .distinct()
                        .sorted()
                }
            )

            configuredRepositories.set(project.provider { project.zapper.repositories.distinct().sorted() })
            includeRepositories.set(project.provider { project.zapper.includeProjectRepositories })

            librariesFolder.set(project.provider { project.zapper.libsFolder })

            relocationList.set(
                project.provider {
                    project.zapper.relocations
                        .map { "${it.pattern}:${project.zapper.relocationPrefix}.${it.newPattern}" }
                        .distinct()
                        .sorted()
                }
            )

            relocationPrefix.set(project.provider { project.zapper.relocationPrefix })
        }

        project.tasks.withType(Jar::class.java).configureEach {
            dependsOn(generateZapperFiles)

            from(outputDirFile) {
                include("dependencies.txt")
                include("relocations.txt")
                include("repositories.txt")
                include("zapper.properties")
                into("zapper")
            }
        }
    }
}

private fun configureShadowRelocations(project: Project) {
    project.plugins.withId("com.gradleup.shadow") {
        project.tasks.withType<ShadowJar>().configureEach {
            val extension = project.zapper

            extension.relocations.forEach {
                relocate(it.pattern, "${extension.relocationPrefix}.${it.newPattern}")
            }

            relocate("revxrsal.zapper", "${extension.relocationPrefix}.zapper")
        }
    }
}