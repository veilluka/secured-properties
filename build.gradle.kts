import org.jetbrains.kotlin.fir.expressions.builder.buildArgumentList
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinjvm:String by extra
val version: String by extra
val versionJavafx: String by extra

plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.14"
    id ("org.javamodularity.moduleplugin") version "1.8.12"
    kotlin("jvm") version "2.0.0"
    distribution
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

val compileJava: JavaCompile by tasks

val appVersion: String = property("version") as String

application {
    mainModule.set("ch.vilki.secured")
    mainClass.set("ch.vilki.secured.Console")
}


repositories {
    mavenCentral()
}

modularity{
   moduleVersion(appVersion)
}

sourceSets {
    main {
        java {
            srcDir(File(buildDir, "generated/sources/version/java"))
        }
    }
}

dependencies {
    //implementation("org.jfxtras:jmetro:11.6.16")
    implementation( "org.apache.logging.log4j:log4j-core:2.11.1")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.peter-gergely-horvath:windpapi4j:1.1.0")
    implementation("commons-cli:commons-cli:1.5.0")
    implementation( "org.bouncycastle:bcprov-jdk16:1.45")
    //implementation("org.controlsfx:controlsfx:11.1.2")
    implementation("com.google.guava:guava:32.1.1-jre")
    implementation("commons-codec:commons-codec:1.16.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")

}

tasks.register("generateVersionClass") {
    doLast {
        val version = findProperty("version")?.toString() ?: "1.0.0"
        val content = """
            package ch.vilki.secured;

            public class ApplicationVersion {
                public static final String VERSION = "$version";
            }
        """.trimIndent()
        val dir = File(projectDir, "src/main/java/ch/vilki/secured")
        dir.mkdirs() // Create the directory if it doesn't exist
        File(dir, "ApplicationVersion.java").writeText(content)
    }
}


// Optionally, ensure that this task runs before compiling Java sources
tasks.named("compileJava") {
    dependsOn("generateVersionClass")
}

javafx {
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.base")
    version = versionJavafx
}

distributions {
    main {
        contents {
            from("README.adoc")
        }
    }
}

tasks.compileTestJava {
    enabled = false
}

// Ensure that the generateVersionClass task is executed


val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "21"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "21"
}

// Custom task to run the GUI  
tasks.register<Exec>("runGui") {
    group = "application"
    description = "Run the application in GUI mode"
    commandLine("cmd", "/c", "gradlew.bat", "run", "--args=-gui")
    workingDir = projectDir
}

