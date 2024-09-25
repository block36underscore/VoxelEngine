import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    kotlin("multiplatform") version "2.0.20"
    application
    kotlin("plugin.serialization") version "2.0.20"
}

group = "gay.block36"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://raw.githubusercontent.com/kotlin-graphics/mary/master")
}

val lwjglVersion = "3.3.4"
val jomlVersion = "1.10.7"
val `joml-primitivesVersion` = "1.10.0"
val lwjglNatives = "natives-linux"

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.7.1")

//    implementation(project(":kgl:kgl-core"))
//    implementation(project(":kgl:kgl-glfw"))
//    implementation(project(":kgl:kgl-glfw-static"))
//    implementation(project(":kgl:kgl-vulkan"))
//    implementation(project(":kgl:kgl-glfw-vulkan"))
//    implementation(project(":kgl:kgl-stb"))

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
    jvm()
}