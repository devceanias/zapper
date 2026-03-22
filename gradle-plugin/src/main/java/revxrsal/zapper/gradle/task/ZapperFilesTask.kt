package revxrsal.zapper.gradle.task

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class ZapperFilesTask : DefaultTask() {
    @get:Input
    abstract val zapDependencies: ListProperty<String>

    @get:Classpath
    abstract val resourceClasspath: ConfigurableFileCollection

    @get:Input
    abstract val projectRepositories: ListProperty<String>

    @get:Input
    abstract val configuredRepositories: ListProperty<String>

    @get:Input
    abstract val includeRepositories: Property<Boolean>

    @get:Input
    abstract val librariesFolder: Property<String>

    @get:Input
    abstract val relocationList: ListProperty<String>

    @get:Input
    abstract val relocationPrefix: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputPath = outputDirectory.get().asFile.toPath()

        Files.createDirectories(outputPath)

        outputPath.resolve("repositories.txt").writeLines(collectRepositories())
        outputPath.resolve("relocations.txt").writeLines(collectRelocations())
        outputPath.resolve("dependencies.txt").writeLines(collectDependencies())

        outputPath.resolve("zapper.properties").writeText(
            """
            libs-folder=${librariesFolder.get()}
            relocation-prefix=${relocationPrefix.get()}
            """.trimIndent()
        )
    }

    private fun collectDependencies(): List<String> {
        val dependencies = linkedSetOf<String>()

        zapDependencies.get().forEach(dependencies::add)

        collectResource("dependencies.txt").forEach(dependencies::add)

        return dependencies.toList()
    }

    private fun collectRelocations(): List<String> {
        val values = linkedSetOf<String>()

        relocationList.get().forEach(values::add)

        collectResource("relocations.txt").forEach(values::add)

        return values.toList()
    }

    private fun collectRepositories(): List<String> {
        val values = linkedSetOf<String>()

        configuredRepositories.get().forEach(values::add)

        if (includeRepositories.get()) {
            projectRepositories.get().forEach(values::add)
        }

        collectResource("repositories.txt").forEach(values::add)

        return values.toList()
    }

    private fun collectResource(name: String): List<String> {
        val collected = linkedSetOf<String>()

        resourceClasspath.files.forEach { file ->
            if (!file.isFile || file.extension != "jar") {
                return@forEach
            }

            JarFile(file).use { jar ->
                val entry = jar.getJarEntry("zapper/$name") ?: return@use

                jar.getInputStream(entry).bufferedReader().useLines { lines ->
                    lines.filter(String::isNotBlank).forEach(collected::add)
                }
            }
        }

        return collected.toList()
    }

    private fun Path.writeLines(lines: List<String>) {
        Files.write(
            this, lines.joinToString("\n").toByteArray(StandardCharsets.UTF_8)
        )
    }

    private fun Path.writeText(content: String) {
        Files.write(this, content.toByteArray(StandardCharsets.UTF_8))
    }
}