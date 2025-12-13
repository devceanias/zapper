plugins {
    id("java")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<Jar>().configureEach {
    archiveBaseName.set("zapper")
}

mavenPublishing {
    coordinates(
        groupId = rootProject.group as String,
        artifactId = "zapper",
        version = rootProject.version as String
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
