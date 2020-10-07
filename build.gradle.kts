group = "moe.lemonneko"
version = "0.1.${"git rev-list HEAD --count".execute()}-KTOR"

fun String.execute(): String {
    val process = Runtime.getRuntime().exec(this)
    return String(process.inputStream.buffered().readBytes()).trim()
}

val kotlin_version: String by project
val ktor_version: String by project

application {
    mainClassName = "moe.lemonneko.gdh.GDHApplicationKt"
}

plugins {
    application
    kotlin("jvm").version("1.4.0")
    id("com.github.johnrengelman.shadow").version("6.0.0")
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://kotlin.bintray.com/ktor")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")

    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-sessions:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")

    implementation("io.ktor:ktor-client-okhttp:$ktor_version")

    implementation("moe.lemonneko:neko-logger-common:2.0.47-SNAPSHOT")
    implementation("moe.lemonneko:neko-git-common:1.0.7-SNAPSHOT")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha1")

    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes("Main-Class" to application.mainClassName)
    }
}