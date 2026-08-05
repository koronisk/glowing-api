plugins {
    kotlin("jvm") version "2.3.21"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "ru.let.glowingapi"
version = project.findProperty("pluginVersion")!!

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    flatDir { dirs("libs") }
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
    compileOnly(files("libs/kotlinstdlib-2.3.21-all.jar"))
    paperweight.paperDevBundle("26.2.build.+")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}