import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    id("org.springframework.boot") version "2.3.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("jvm") version "1.4.0"
    kotlin("plugin.spring") version "1.4.0"
}

fun String.execute(): String {
    val process = Runtime.getRuntime().exec(this)
    return String(process.inputStream.buffered().readBytes()).trim()
}

group = "moe.nekonest"
version = "0.2.${"git rev-list HEAD --count".execute()}"

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenLocal()
    maven {
        url = URI.create("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    maven {
        url = URI.create("https://maven.aliyun.com/repository/public")
    }
}

dependencies {
    implementation("org.springframework.session:spring-session:1.3.5.RELEASE")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.7.0.202003110725-r")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.alibaba:fastjson:1.2.70")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    implementation("moe.lemonneko:neko-logger-common:2.0.47-SNAPSHOT")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.getByName("bootJar").doLast("generateDockerfile") {
    val content = file("Dockerfile.template")
            .readText().replace("%version".toRegex(), project.version as String)
    file("Dockerfile").writeText(content)
}