package gay.block36.voxel

import gay.block36.voxel.vulkan.DebugMessenger
import gay.block36.voxel.vulkan.VulkanInfo
import gay.block36.voxel.vulkan.destroyDebugUtilsMessengerEXT
import gay.block36.voxel.vulkan.initVulkan
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkInstance
import kotlin.properties.Delegates


fun main() {
    try {
        initWindow()
        initVulkan()

        mainLoop@ while (true) {
            if (glfwWindowShouldClose(Window)) break@mainLoop

            glfwPollEvents()
        }
    } finally {
        cleanup()
    }
}

object WindowInfo {
    const val WIDTH = 1920
    const val HEIGHT = 1080
    const val TITLE = "Voxel Engine"
}

var Window: Long by Delegates.notNull()
lateinit var Instance: VkInstance

private fun initWindow() {
    if (!glfwInit()) throw RuntimeException("Cannot init GLFW")

    glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

    Window = glfwCreateWindow(
        WindowInfo.WIDTH,
        WindowInfo.HEIGHT,
        WindowInfo.TITLE,
        NULL,
        NULL)

    if (Window == NULL) throw RuntimeException("Cannot create window")
}

private fun cleanup() {
    if (VulkanInfo.VALIDATION_LAYERS_ENABLED)
        destroyDebugUtilsMessengerEXT(Instance, DebugMessenger, null)

    vkDestroyInstance(Instance, null)

    glfwDestroyWindow(Window)

    glfwTerminate()
}