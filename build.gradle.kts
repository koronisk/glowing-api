plugins {
    kotlin("jvm") version "2.3.21"
    id("com.gradleup.shadow") version "9.6.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "ru.let.glowingapi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    paperweight.paperDevBundle("26.2.build.+")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "ru.let.glowingapi.GlowingApiPlugin"
    }
}