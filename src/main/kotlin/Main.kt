package gay.block36.voxel

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkApplicationInfo
import org.lwjgl.vulkan.VkInstance
import org.lwjgl.vulkan.VkInstanceCreateInfo
import kotlin.properties.Delegates


fun main() {
    initWindow()
    initVulkan()

    mainLoop@while (true) {
        if (glfwWindowShouldClose(Window)) break@mainLoop

        glfwPollEvents()

        Thread.sleep(WindowInfo.LOOP_PERIOD)
    }

    cleanup()
}

object WindowInfo {
    const val WIDTH = 1920
    const val HEIGHT = 1080
    const val TITLE = "Voxel Engine"
    const val LOOP_PERIOD = 1000L/60L
}

var Window: Long by Delegates.notNull()
lateinit var Instance: VkInstance

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
    MemoryStack.stackPush().run {
        val appInfo = VkApplicationInfo.calloc(this).apply {
            sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
            pApplicationName(this@run.UTF8Safe(WindowInfo.TITLE))
            applicationVersion(VK_MAKE_VERSION(1, 0, 0))
            pEngineName(this@run.UTF8Safe("No Engine"))
            engineVersion(VK_MAKE_VERSION(1, 0, 0))
            apiVersion(VK_API_VERSION_1_0)
        }

        val createInfo = VkInstanceCreateInfo.calloc(this).apply {
            sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
            pApplicationInfo(appInfo)
            ppEnabledExtensionNames(glfwGetRequiredInstanceExtensions())
            ppEnabledLayerNames(null)
        }

        val instancePtr = this.mallocPointer(1)

        if (vkCreateInstance(createInfo, null, instancePtr) != VK_SUCCESS) {
            throw RuntimeException("Failed to create vulkan instance")
        }

        Instance = VkInstance(instancePtr.get(0), createInfo)
    }
}

private fun cleanup() {

    vkDestroyInstance(Instance, null)

    glfwDestroyWindow(Window)

    glfwTerminate()
}