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
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives, version = lwjglVersion)
    runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNatives, version = lwjglVersion)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives, version = lwjglVersion)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives, version = lwjglVersion)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives, version = lwjglVersion)
    runtimeOnly("org.lwjgl", "lwjgl-vma", classifier = lwjglNatives, version = lwjglVersion)
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


tasks.register("compileShaders") {
    println("test: ${temporaryDir.absolutePath}")

    doLast {

        sourceSets.filter { it.name != "test" }.forEach {
            it.resources.sourceDirectories.forEach { resourcesDir ->
                File("${resourcesDir.absolutePath}/assets").listFiles()
                    ?.filter { namespace -> namespace.isDirectory }
                    ?.forEach { namespace ->
                        File(namespace, "/shaders").compileShaderFiles(namespace.name)
                            .map { file ->
                                File(
                                    "${namespace.path}/shaders",
                                    "/${file.split(":")[1]}"
                                ) to File(
                                    temporaryDir,
                                    "${file.split(":")[0]}/${file.split(":")[1].split(".")[0]}.spv"
                                )
                            }
                            .forEach { files ->
                                files.second.parentFile.mkdirs()
                                ProcessBuilder(
                                    listOf(
                                        "naga",
                                        files.first.path,
                                        files.second.path,
                                    )
                                ).redirectOutput(ProcessBuilder.Redirect.INHERIT)
                                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                                    .start()
                                    .waitFor()

                                copy {
                                    from("$rootDir/build/tmp/compileShaders/${namespace.name}") {
                                        println(files.second.absolutePath.removePrefix("$rootDir/build/tmp/compileShaders/${namespace.name}/"))
                                        include(files.second.absolutePath.removePrefix("$rootDir/build/tmp/compileShaders/${namespace.name}/"))
                                    }
                                    into("$rootDir/build/resources/main/assets/${namespace.name}/shaders")
                                }
                            }
                    }
            }
        }
    }
}

fun File.compileShaderFiles(namespace: String, pathIn: String = ""): ArrayList<String> {
    val path = pathIn.removePrefix("/")
    val output = ArrayList<String>()
    listFiles()
        ?.filter(File::isDirectory)
        ?.forEach { output.addAll(it.compileShaderFiles(namespace, "$path/${it.name}")) }

    listFiles()
        ?.filter(File::isFile)
        ?.filter {
            arrayOf("wgsl").contains(it.extension)
        }?.forEach {
            output.add("${namespace}:$path${if (path != "") "/" else ""}${it.name}")
        }

    return output
}


tasks.processResources {
    dependsOn(tasks.getByName("compileShaders"))
}