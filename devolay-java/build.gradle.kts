import org.gradle.internal.jvm.Jvm
import org.gradle.api.JavaVersion

import com.jfrog.bintray.gradle.BintrayExtension

import java.io.FileReader
import java.util.Properties
import java.util.Date

plugins {
    base
    `java-library`
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
}

base.archivesBaseName = "devolay"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val sourceJar by tasks.creating(Jar::class) {
    from(sourceSets.main.get().allJava)
    this.archiveClassifier.set("sources")
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn("javadoc")
    from(tasks.javadoc.get().destinationDir)
    this.archiveClassifier.set("javadoc")
}

// Load keys.properties and configure bintray for deploy
val keysProperties = Properties()
if(project.file("keys.properties").exists()) {
    FileReader(project.file("keys.properties")).use { reader ->
        keysProperties.load(reader)
    }
}

println(keysProperties.getProperty("bintray_user"))

bintray {
    user = keysProperties.getProperty("bintray_user")
    key = keysProperties.getProperty("bintray_api_key")

    setPublications("Devolay")

    pkg (delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "devolay"
        name = "devolay"
        vcsUrl = "https://github.com/WalkerKnapp/devolay.git"
        setLicenses("Apache-2.0")

        version (delegateClosureOf<BintrayExtension.VersionConfig> {
            name = project.version as String?
            desc = "Devolay Library " + project.version + " Release"
            released = Date().toString()
            vcsTag = "v" + project.version
        })
    })
}

publishing {
    publications {
        create<MavenPublication>("Devolay") {
            from(components["java"])
            artifact(sourceJar)
            artifact(javadocJar)

            groupId = project.group as String
            artifactId = "devolay"
            version = project.version as String?

            pom {
                name.set("devolay")
                description.set("A Java binding for the Newtek NDI(tm) SDK.")
                url.set("https://github.com/WalkerKnapp/devolay")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("WalkerKnapp")
                        name.set("Walker Knapp")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/WalkerKnapp/devolay.git")
                    developerConnection.set("scm:git:git@github.com:WalkerKnapp/devolay.git")
                    url.set("https://github.com/WalkerKnapp/devolay")
                }
            }
        }
    }
}

tasks.register<Exec>("generateJniHeaders") {
    description = "Generates C headers for JNI by running javah"
    group = "build"

    val nativeIncludes = "../devolay-natives/src/main/headers"

    val javacCommand = mutableListOf(Jvm.current().javacExecutable.toString(),
        "-d",
        "build/tmp/generateJniHeaders",
        "-h",
        nativeIncludes)

    sourceSets.main.get().allJava.onEach { file ->
        javacCommand.add(file.toPath().toAbsolutePath().toString())
    }

    commandLine(javacCommand)
}

tasks.processResources {
    dependsOn(":devolay-natives:assemble")
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources", project(":devolay-natives").buildDir.absolutePath + "/lib/main/debug")
        }
    }
}
