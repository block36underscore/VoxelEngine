package gay.block36.voxel.vulkan

import gay.block36.voxel.Instance
import gay.block36.voxel.WindowInfo
import gay.block36.voxel.vulkan.VulkanInfo.VALIDATION_LAYERS
import org.lwjgl.PointerBuffer
import org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions
import org.lwjgl.system.Configuration.DEBUG
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.EXTDebugUtils.*
import org.lwjgl.vulkan.VK10.*
import java.nio.LongBuffer
import kotlin.properties.Delegates


var DebugMessenger: Long by Delegates.notNull()

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
}

private fun createInstance() {
    if (VulkanInfo.VALIDATION_LAYERS_ENABLED && !validationLayersSupported())
        throw RuntimeException("Validation layers are required, but not enabled")

    MemoryStack.stackPush().use { stack ->
        val appInfo = VkApplicationInfo.calloc(stack).apply {
            sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
            pApplicationName(stack.UTF8Safe(WindowInfo.TITLE))
            applicationVersion(VK_MAKE_VERSION(1, 0, 0))
            pEngineName(stack.UTF8Safe("No Engine"))
            engineVersion(VK_MAKE_VERSION(1, 0, 0))
            apiVersion(VK_API_VERSION_1_0)
        }

        val createInfo = VkInstanceCreateInfo.calloc(stack).apply {
            sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
            pApplicationInfo(appInfo)
            ppEnabledExtensionNames(getRequiredExtensions(stack))
            ppEnabledLayerNames(null)

            if (VulkanInfo.VALIDATION_LAYERS_ENABLED && VALIDATION_LAYERS != null) {
                ppEnabledLayerNames(
                    stack.mallocPointer(VALIDATION_LAYERS.size)
                        .apply(
                            layers@ fun PointerBuffer.() {
                                VALIDATION_LAYERS
                                    .mapNotNull(stack::UTF8Safe)
                                    .forEach(this@layers::put)
                            }
                        )
                )
            }
        }

        val instancePtr = stack.mallocPointer(1)

        if (vkCreateInstance(createInfo, null, instancePtr) != VK_SUCCESS) {
            throw RuntimeException("Failed to create vulkan instance")
        }

        Instance = VkInstance(instancePtr[0], createInfo)
    }
}

fun getRequiredExtensions(stack: MemoryStack): PointerBuffer {
    val glfwExtensions = glfwGetRequiredInstanceExtensions()!!

    if (VulkanInfo.VALIDATION_LAYERS_ENABLED) {
        val extensions = stack.mallocPointer(glfwExtensions.capacity() + 1).apply {
            put(glfwExtensions)
            put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME))
        }

        return extensions.rewind()
    }

    return glfwExtensions
}

fun debugCallback(
    severity: Int,
    type: Int,
    callBackData: Long,
    userData: Long
): Int {
    System.err.println("Validation layer: ${VkDebugUtilsMessengerCallbackEXT.create(callBackData)}")

    return VK_FALSE
}

fun populateDebugMessengerCreateInfo(debugCreateInfo: VkDebugUtilsMessengerCreateInfoEXT) {
    debugCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
    debugCreateInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
    debugCreateInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
    debugCreateInfo.pfnUserCallback(::debugCallback)
}

fun setupDebugMessenger() {
    if (!VulkanInfo.VALIDATION_LAYERS_ENABLED) return

    MemoryStack.stackPush().use { stack ->
        val createInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack)

        populateDebugMessengerCreateInfo(createInfo)
        
        val debugMessenger = stack.longs(VK_NULL_HANDLE)
        
        if (createDebugUtilsMessengerEXT(Instance, createInfo, null, debugMessenger) != VK_SUCCESS)
            throw RuntimeException("Failed to setup debug messenger")
        
        DebugMessenger = debugMessenger[0]
    }
}

fun createDebugUtilsMessengerEXT(
    instance: VkInstance, createInfo: VkDebugUtilsMessengerCreateInfoEXT,
    allocationCallbacks: VkAllocationCallbacks?, pDebugMessenger: LongBuffer
): Int {
    if (vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") != NULL) {
        return vkCreateDebugUtilsMessengerEXT(instance, createInfo, allocationCallbacks, pDebugMessenger)
    }

    return VK_ERROR_EXTENSION_NOT_PRESENT
}

fun destroyDebugUtilsMessengerEXT(
    instance: VkInstance,
    debugMessenger: Long,
    allocationCallbacks: VkAllocationCallbacks?
) {
    if (vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT") != NULL) {
        vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, allocationCallbacks)
    }
}

fun validationLayersSupported(): Boolean {
    MemoryStack.stackPush().use { stack ->
        val layerCount = stack.ints(0)

        vkEnumerateInstanceLayerProperties(layerCount, null)

        val availableLayers = VkLayerProperties.malloc(layerCount[0], stack)

        vkEnumerateInstanceLayerProperties(layerCount, availableLayers)

        val availableLayerNames = availableLayers
            .map(VkLayerProperties::layerNameString)

        return VALIDATION_LAYERS?.let { validationLayers ->
            availableLayerNames.containsAll(validationLayers).also {
                println("Available Layers: ")
                availableLayerNames.forEach(::println)
            }
        } ?: true
    }
}