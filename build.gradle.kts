plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "dev.mizarc"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("xyz.jpenilla:squaremap-api:1.3.9")

    // Depend on WaystoneWarps - you'll need to add the JAR to libs/ folder or install to local maven
    compileOnly(files("libs/WaystoneWarps-0.3.4.jar"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.shadowJar {
    archiveClassifier.set("")

    // Relocate kotlin stdlib if needed
    relocate("kotlin", "org.jamesphbennett.waystonewarps.squaremap.libs.kotlin")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
