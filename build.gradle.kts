import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    kotlin("jvm") version "2.0.20"
    application
    kotlin("plugin.serialization") version "2.0.20"
}

group = "gay.block36"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val useSingleTarget: Boolean by extra { System.getProperty("idea.active") == "true" }
val lwjglVersion: String by extra("3.3.4")
val lwjglNatives: String by extra {
    when {
        HostManager.hostIsLinux -> "natives-linux"
        HostManager.hostIsMac -> "natives-macos"
        HostManager.hostIsMingw -> "natives-windows"
        else -> error("Host platform not supported")
    }
}

val jomlVersion = "1.10.7"
val `joml-primitivesVersion` = "1.10.0"

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.7.1")

    implementation("io.github.kotlin-graphics:kotlin-unsigned:3.3.32")

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-assimp")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-stb")
    implementation("org.lwjgl", "lwjgl-vma")
    implementation("org.lwjgl", "lwjgl-vulkan")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-vma", classifier = lwjglNatives)
    implementation("org.joml", "joml", jomlVersion)
    implementation("org.joml", "joml-primitives", `joml-primitivesVersion`)

}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "gay.block36.voxel.MainKt"
}

kotlin {
    jvmToolchain(21)
}