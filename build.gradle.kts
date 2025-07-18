import com.github.gradle.node.npm.task.NpmTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val spaceSdkVersion: String by project
val ktorVersion: String by project
val logbackVersion: String by project
val exposedVersion: String by project
val hikariVersion: String by project
val postgresqlDriverVersion: String by project
val nimbusVersion: String by project
val kotlinxSerializationVersion: String by project

plugins {
    application
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.0"
    id("docker-compose")
    id("com.github.node-gradle.node") version "3.4.0"
    id("com.google.cloud.tools.jib") version "3.3.1"
}

jib {
    container {
        mainClass = "io.ktor.server.jetty.EngineMain"
        ports = listOf("8080")
    }
}

node {
    version.set("16.15.1")
    download.set(true)
    nodeProjectDir.set(file("${project.projectDir}/client"))
}

repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/crush/maven")
    maven("https://maven.pkg.jetbrains.space/public/p/space/maven")
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-jetty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-locations-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-server-caching-headers:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    implementation("org.jetbrains:space-sdk-jvm:$spaceSdkVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.postgresql:postgresql:$postgresqlDriverVersion")

    implementation("com.nimbusds:nimbus-jose-jwt:$nimbusVersion")

    testImplementation(kotlin("test"))
}

kotlin.sourceSets.all {
    languageSettings {
        optIn("kotlin.time.ExperimentalTime")
        optIn("io.ktor.server.locations.KtorExperimentalLocationsAPI")
        optIn("space.jetbrains.api.ExperimentalSpaceSdkApi")
    }
}

sourceSets {
    main {
        resources {
            srcDirs("client/dist")
        }
    }
}

dockerCompose {
    projectName = "space-events"
    removeContainers = false
    removeVolumes = false
}

tasks.register("buildClient", NpmTask::class) {
    npmCommand.set(listOf("run", "build"))

    dependsOn("npmInstall")
}


tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    val run by getting(JavaExec::class)
    dockerCompose.isRequiredBy(run)

    val distZip by existing {
        dependsOn("buildClient")
    }

    val jib by existing {
        dependsOn("buildClient")
    }

    val jibDockerBuild by existing {
        dependsOn("buildClient")
    }
}

application {
    mainClass.set("io.ktor.server.jetty.EngineMain")
}
