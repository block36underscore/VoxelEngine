package gay.block36.voxel.vulkan

import gay.block36.voxel.WindowInfo
import org.lwjgl.PointerBuffer
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*

fun createInstance() {
    if (VulkanInfo.VALIDATION_LAYERS_ENABLED && !validationLayersSupported())
        throw RuntimeException("Validation layers are required, but not enabled")

    MemoryStack.stackPush().use { stack ->
        val appInfo = VkApplicationInfo.calloc(stack).apply {
            sType(VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO)
            pApplicationName(stack.UTF8Safe(WindowInfo.TITLE))
            applicationVersion(VK10.VK_MAKE_VERSION(1, 0, 0))
            pEngineName(stack.UTF8Safe("No Engine"))
            engineVersion(VK10.VK_MAKE_VERSION(1, 0, 0))
            apiVersion(VK10.VK_API_VERSION_1_0)
        }

        val createInfo = VkInstanceCreateInfo.calloc(stack).apply {
            sType(VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
            pApplicationInfo(appInfo)
            ppEnabledExtensionNames(getRequiredExtensions(stack))

            if (VulkanInfo.VALIDATION_LAYERS_ENABLED && VulkanInfo.VALIDATION_LAYERS != null) {
                ppEnabledLayerNames(validationLayersAsPointerBuffer(stack))

                val debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack)
                populateDebugMessengerCreateInfo(debugCreateInfo)
                pNext(debugCreateInfo.address())
            }
        }

        val instancePtr = stack.mallocPointer(1)

        if (VK10.vkCreateInstance(createInfo, null, instancePtr) != VK10.VK_SUCCESS) {
            throw RuntimeException("Failed to create vulkan instance")
        }

        Instance = VkInstance(instancePtr[0], createInfo)
    }
}

fun getRequiredExtensions(stack: MemoryStack): PointerBuffer {
    val glfwExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions()!!

    if (VulkanInfo.VALIDATION_LAYERS_ENABLED) {
        val extensions = stack.mallocPointer(glfwExtensions.capacity() + 1).apply {
            put(glfwExtensions)
            put(stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME))
        }

        return extensions.rewind()
    }

    return glfwExtensions
}