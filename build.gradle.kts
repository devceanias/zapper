import com.vanniktech.maven.publish.SonatypeHost

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

group = "io.github.revxrsal"
version = "1.0.2"

subprojects {

    val isExample = project.path.startsWith(":example")

    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")

    if (!isExample)
        apply(plugin = "com.vanniktech.maven.publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }

    if (!isExample) {
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
                url.set("https://github.com/Revxrsal/Zapper/")
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
                    url.set("https://github.com/Revxrsal/Zapper/")
                    connection.set("scm:git:git://github.com/Revxrsal/Zapper.git")
                    developerConnection.set("scm:git:ssh://git@github.com/Revxrsal/Zapper.git")
                }
            }

            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
            signAllPublications()
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}