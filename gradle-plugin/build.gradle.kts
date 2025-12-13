plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:9.2.2")
}

gradlePlugin {
    plugins {
        create("zapper") {
            id = "net.oceanias.zapper"
            displayName = "Zapper"
            description = "A fork of the powerful and flexible Maven runtime dependency downloader. Original: https://github.com/Revxrsal/Zapper"
            implementationClass = "revxrsal.zapper.gradle.ZapperPlugin"
            website = "https://github.com/devceanias/zapper"
            vcsUrl = "https://github.com/devceanias/zapper"
            tags = listOf("maven", "downloader", "runtime dependency")
        }
    }
}
