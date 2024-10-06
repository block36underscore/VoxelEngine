package gay.block36.voxel.vulkan

import gay.block36.voxel.vulkan.DebugCallback.debugCallback
import gay.block36.voxel.vulkan.VulkanInfo.VALIDATION_LAYERS
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*
import java.nio.LongBuffer
import kotlin.properties.Delegates


var DebugMessenger: Long by Delegates.notNull()


fun populateDebugMessengerCreateInfo(debugCreateInfo: VkDebugUtilsMessengerCreateInfoEXT) {
    debugCreateInfo.sType(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
    debugCreateInfo.messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT or EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT or EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
    debugCreateInfo.messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT or EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT or EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
    debugCreateInfo.pfnUserCallback(::debugCallback)
}

fun setupDebugMessenger() {
    if (!VulkanInfo.VALIDATION_LAYERS_ENABLED) return

    MemoryStack.stackPush().use { stack ->
        val createInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack)

        populateDebugMessengerCreateInfo(createInfo)

        val debugMessenger = stack.longs(VK10.VK_NULL_HANDLE)

        if (createDebugUtilsMessengerEXT(Instance, createInfo, null, debugMessenger) != VK10.VK_SUCCESS)
            throw RuntimeException("Failed to setup debug messenger")

        DebugMessenger = debugMessenger[0]
    }
}

fun createDebugUtilsMessengerEXT(
    instance: VkInstance, createInfo: VkDebugUtilsMessengerCreateInfoEXT,
    allocationCallbacks: VkAllocationCallbacks?, pDebugMessenger: LongBuffer
): Int {
    if (VK10.vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") != MemoryUtil.NULL) {
        return EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance, createInfo, allocationCallbacks, pDebugMessenger)
    }

    return VK10.VK_ERROR_EXTENSION_NOT_PRESENT
}

fun destroyDebugUtilsMessengerEXT(
    instance: VkInstance,
    debugMessenger: Long,
    allocationCallbacks: VkAllocationCallbacks?
) {
    if (VK10.vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT") != MemoryUtil.NULL) {
        EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, allocationCallbacks)
    }
}

fun validationLayersSupported(): Boolean {
    MemoryStack.stackPush().use { stack ->
        val layerCount = stack.ints(0)

        VK10.vkEnumerateInstanceLayerProperties(layerCount, null)

        val availableLayers = VkLayerProperties.malloc(layerCount[0], stack)

        VK10.vkEnumerateInstanceLayerProperties(layerCount, availableLayers)

        val availableLayerNames = availableLayers
            .map(VkLayerProperties::layerNameString)

        return VulkanInfo.VALIDATION_LAYERS?.let { validationLayers ->
            availableLayerNames.containsAll(validationLayers).also { available ->
                if (!available) {
                    println("Available Layers:")
                    availableLayerNames.forEach(::println)
                    println("Needed layers:")
                    validationLayers.forEach(::println)
                }
            }
        } ?: true
    }
}