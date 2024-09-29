package gay.block36.voxel

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryUtil.NULL
import kotlin.properties.Delegates


fun main() {
    initWindow()

    mainLoop@while (true) {
        if (glfwWindowShouldClose(Window)) break@mainLoop

        glfwPollEvents()

        Thread.sleep(WindowInfo.LOOP_PERIOD)
    }
}

object WindowInfo {
    const val WIDTH = 1920
    const val HEIGHT = 1080
    const val TITLE = "Voxel Engine"
    const val LOOP_PERIOD = 1000L/60L
}

var Window: Long by Delegates.notNull()

private fun initWindow() {
    if (!glfwInit()) {
        throw RuntimeException("Cannot init GLFW")
    }

    glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

    Window = glfwCreateWindow(
        WindowInfo.WIDTH,
        WindowInfo.HEIGHT,
        WindowInfo.TITLE,
        NULL,
        NULL)

    if (Window == NULL) {
        throw RuntimeException("Cannot create window");
    }
}

private fun initVulkan() {

}

private fun cleanup() {
    glfwDestroyWindow(Window)

    glfwTerminate()
}