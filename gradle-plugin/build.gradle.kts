plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.1"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly("com.github.johnrengelman.shadow:com.github.johnrengelman.shadow.gradle.plugin:8.1.1")
}

gradlePlugin {
    plugins {
        create("zapper") {
            id = "net.oceanias.zapper"
            displayName = "Zapper"
            description = "A powerful and flexible Maven dependency downloader at runtime"
            implementationClass = "revxrsal.zapper.gradle.ZapperPlugin"
            website = "https://github.com/Revxrsal/Zapper"
            vcsUrl = "https://github.com/Revxrsal/Zapper"
            tags = listOf("maven", "downloader", "runtime dependency")
        }
    }
}
