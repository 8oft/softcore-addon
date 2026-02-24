plugins {
    alias(libs.plugins.fabric.loom)
}

// Version configuration
val mcVersions = listOf("1.21.4", "1.21.11")
val selectedVersion = (project.findProperty("mcVersion") as String?) ?: "1.21.11"

base {
    archivesName = properties["archives_base_name"] as String
    version = "${libs.versions.mod.version.get()}-${selectedVersion}"
    group = properties["maven_group"] as String
}

// Version mapping
val versionMap = mapOf(
    "1.21.4" to mapOf(
        "yarn" to "1.21.4+build.2",
        "meteor" to "1.21.4-SNAPSHOT"
    ),
    "1.21.11" to mapOf(
        "yarn" to "1.21.11+build.3",
        "meteor" to "1.21.11-SNAPSHOT"
    )
)

val mcVersion = selectedVersion
val yarnVersion = versionMap[mcVersion]?.get("yarn") ?: "1.21.11+build.3"
val meteorVersion = versionMap[mcVersion]?.get("meteor") ?: "1.21.11-SNAPSHOT"

repositories {
    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "meteor-maven-snapshots"
        url = uri("https://maven.meteordev.org/snapshots")
    }
}

dependencies {
    // Fabric
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(variantOf("net.fabricmc:yarn:$yarnVersion") { classifier("v2") })
    modImplementation("net.fabricmc:fabric-loader:${libs.versions.fabric.loader.get()}")

    // Meteor
    modImplementation("meteordevelopment:meteor-client:$meteorVersion")
}

tasks {
    processResources {
        val propertyMap = mapOf(
            "version" to project.version,
            "mc_version" to selectedVersion
        )

        inputs.properties(propertyMap)

        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(propertyMap)
        }
    }

    jar {
        inputs.property("archivesName", project.base.archivesName.get())

        from("LICENSE") {
            rename { "${it}_${inputs.properties["archivesName"]}" }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 21
        options.compilerArgs.add("-Xlint:deprecation")
        options.compilerArgs.add("-Xlint:unchecked")
    }

    // Build tasks for each version
    register("build1_21_4") {
        description = "Build for Minecraft 1.21.4"
        dependsOn(":clean", "build")
    }

    register("build1_21_11") {
        description = "Build for Minecraft 1.21.11"
        dependsOn(":clean", "build")
    }

    register("buildAll") {
        description = "Build for all supported versions (1.21.4 and 1.21.11)"
        doLast {
            println("Building Softcore Addon v${libs.versions.mod.version.get()} for all versions...")
            
            // Build 1.21.4
            println("\n--- Building for Minecraft 1.21.4 ---")
            project.exec {
                commandLine("./gradlew", "clean", "build", "-PmcVersion=1.21.4")
            }
            
            // Build 1.21.11
            println("\n--- Building for Minecraft 1.21.11 ---")
            project.exec {
                commandLine("./gradlew", "clean", "build", "-PmcVersion=1.21.11")
            }
            
            println("\n✓ Build complete! Check build/libs/ for JARs")
        }
    }
}
