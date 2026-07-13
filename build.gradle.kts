plugins {
    id("java")
    id("dev.architectury.loom") version ("1.10-SNAPSHOT")
    id("architectury-plugin") version ("3.4-SNAPSHOT")
    kotlin("jvm") version "2.2.0"
}

group = "net.ajsdev"
version = "1.2.0.1-1.7.3"
var minecraft_version = "1.21.1"
var loader_version = "0.17.2"
var fabric_version = "0.116.6+1.21.1"
var cobblemon_version = "1.7.3+1.21.1"

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()

    mixin {
        defaultRefmapName.set("mixins.${project.name}.refmap.json")
    }
}

repositories {
    mavenCentral()
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.nucleoid.xyz")
}

dependencies {
    minecraft("net.minecraft:minecraft:${minecraft_version}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${loader_version}")
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
    modImplementation(fabricApi.module("fabric-command-api-v2", "${fabric_version}"))
    modImplementation("net.fabricmc:fabric-language-kotlin:1.12.3+kotlin.2.0.21")
    modImplementation("com.cobblemon:fabric:${cobblemon_version}")
    modImplementation("eu.pb4:sgui:1.6.1+1.21.1")
}



tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(project.properties)
    }
}