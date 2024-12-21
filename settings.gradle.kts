rootProject.name = "zapper"

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include("api")
include("gradle-plugin")

/*
 * -------- Example projects --------
 */

include("examples")

val exampleProjects = listOf(
    "bukkit-java",
    "bukkit-kotlin",
)

exampleProjects.forEach { project ->
    include("examples:$project")
    findProject(":examples:$project")?.name = project
}
