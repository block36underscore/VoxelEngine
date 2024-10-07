package gay.block36.voxel.vulkan

import gay.block36.voxel.Window
import org.lwjgl.glfw.GLFW
import org.lwjgl.system.Configuration.DEBUG
import org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR
import org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK10.vkDestroyImageView
import org.lwjgl.vulkan.VkInstance


lateinit var Instance: VkInstance

object VulkanInfo {
    val VALIDATION_LAYERS_ENABLED: Boolean = DEBUG.get(true)
    val VALIDATION_LAYERS =
        if (VALIDATION_LAYERS_ENABLED)
            hashSetOf(
                "VK_LAYER_KHRONOS_validation",
            )
        else null
}

fun initVulkan() {
    createInstance()
    setupDebugMessenger()
    createSurface()
    pickPhysicalDevice()
    createLogicalDevice()
    createSwapChain()
    createImageViews()
}

fun cleanup() {

    SwapChainImageViews.forEach {
        vkDestroyImageView(Device, it, null)
    }

    vkDestroySwapchainKHR(Device, SwapChain, null)

    VK10.vkDestroyDevice(Device, null)

    if (::Instance.isInitialized) {
        if (VulkanInfo.VALIDATION_LAYERS_ENABLED)
            destroyDebugUtilsMessengerEXT(Instance, DebugMessenger, null)

        vkDestroySurfaceKHR(Instance, Surface, null)
        VK10.vkDestroyInstance(Instance, null)
    }

    GLFW.glfwDestroyWindow(Window)

    GLFW.glfwTerminate()
}
