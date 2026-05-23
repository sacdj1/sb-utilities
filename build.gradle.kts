plugins {
    id("fabric-loom") version "1.15.5"
    `java-library`
}

loom {
    accessWidenerPath.set(file("src/main/resources/sb-additions.accesswidener"))
}

version = "1.0.0"
group = "com.example"

base { archivesName.set("SB-Utilities") }

repositories {
    maven("https://maven.fabricmc.net")
    maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.shedaniel.me/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings("net.fabricmc:yarn:1.21.11+build.4:v2")
    modImplementation("net.fabricmc:fabric-loader:0.19.0")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.141.3+1.21.11")
    modCompileOnly("com.terraformersmc:modmenu:17.0.0")
    modCompileOnly("me.shedaniel.cloth:cloth-config-fabric:21.11.153")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
