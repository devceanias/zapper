buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.10")
    }
}

plugins {
    id("java")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "net.oceanias.zapper"
version = "1.0.4"

subprojects {
    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")
    apply(plugin = "com.vanniktech.maven.publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    mavenPublishing {
        coordinates(
            groupId = group as String,
            artifactId = "zapper.$name",
            version = version as String
        )
        pom {
            name.set("Zapper")
            description.set("A powerful and flexible Maven dependency downloader at runtime")
            inceptionYear.set("2024")
            url.set("https://github.com/devceanias/zapper")
            licenses {
                license {
                    name.set("MIT")
                    url.set("https://mit-license.org/")
                    distribution.set("https://mit-license.org/")
                }
            }
            developers {
                developer {
                    id.set("revxrsal")
                    name.set("Revxrsal")
                    url.set("https://github.com/Revxrsal/")
                }
            }
            scm {
                url.set("https://github.com/devceanias/zapper")
                connection.set("scm:git:git://github.com/devceanias/zapper.git")
                developerConnection.set("scm:git:ssh://git@github.com/devceanias/zapper.git")
            }
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}