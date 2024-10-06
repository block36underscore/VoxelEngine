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
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")

    }
}

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

    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-assimp:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-openal:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-stb:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-vma:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-vulkan:$lwjglVersion")
    runtimeOnly("org.lwjgl","lwjgl", classifier = lwjglNatives, version = lwjglVersion)
    runtimeOnly("org.lwjgl","lwjgl-assimp", classifier = lwjglNatives, version = lwjglVersion)
    runtimeOnly("org.lwjgl","lwjgl-glfw", classifier = lwjglNatives, version = lwjglVersion)
    runtimeOnly("org.lwjgl","lwjgl-openal", classifier = lwjglNatives, version = lwjglVersion)
    runtimeOnly("org.lwjgl","lwjgl-stb", classifier = lwjglNatives, version = lwjglVersion)
    runtimeOnly("org.lwjgl","lwjgl-vma", classifier = lwjglNatives, version = lwjglVersion)
    implementation("org.joml", "joml", jomlVersion)
    implementation("org.joml", "joml-primitives", `joml-primitivesVersion`)

}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "gay.block36.voxel.MainKt"
//    mainClass = "javavulkantutorial.Ch06SwapChainCreation"
}

kotlin {
    jvmToolchain(21)
}