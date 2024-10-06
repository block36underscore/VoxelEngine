package gay.block36.voxel.vulkan

import gay.block36.voxel.util.asPointerBuffer
import gay.block36.voxel.util.iter
import gay.block36.voxel.vulkan.VulkanInfo.VALIDATION_LAYERS
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR
import org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME
import org.lwjgl.vulkan.VK10.*


lateinit var PhysicalDevice: VkPhysicalDevice
lateinit var Device: VkDevice
lateinit var GraphicsQueue: VkQueue

val DEVICE_EXTENSIONS = listOf(
    VK_KHR_SWAPCHAIN_EXTENSION_NAME,
)

fun pickPhysicalDevice() {
    MemoryStack.stackPush().use { stack ->
        val deviceCount = stack.ints(0)
        vkEnumeratePhysicalDevices(Instance, deviceCount, null)

        if (deviceCount[0] == 0) throw RuntimeException("Failed to find GPUs with vulkan support")

        val physicalDevices = stack.mallocPointer(deviceCount[0])
        vkEnumeratePhysicalDevices(Instance, deviceCount, physicalDevices)

        physicalDevices
            .iter()
            .asSequence()
            .map {
                VkPhysicalDevice(it, Instance)
            }
            .filter(VkPhysicalDevice::isSuitable)
            .take(1)
            .forEach {
                PhysicalDevice = it
                return@use 
            }

        throw RuntimeException("No suitable device found")
    }
}

fun VkPhysicalDevice.isSuitable(): Boolean {
    val indices = this.findQueueFamilies()

    val extensionsSupported: Boolean = this.areExtensionsSupported()
    var swapChainAdequate = false
    if (areExtensionsSupported()) {
        MemoryStack.stackPush().use { stack ->
            querySwapChainSupport(stack).run {
                swapChainAdequate = this.formats.hasRemaining() && this.presentModes.hasRemaining()
            }
        }
    }

    return indices.complete && extensionsSupported && swapChainAdequate
}

fun VkPhysicalDevice.areExtensionsSupported(): Boolean {
    MemoryStack.stackPush().use { stack ->
        val extensionCount = stack.ints(1)

        vkEnumerateDeviceExtensionProperties(
            this,
            null as String?,
            extensionCount,
            null
        )

        val availableExtensions = VkExtensionProperties.malloc(extensionCount[0], stack)
        vkEnumerateDeviceExtensionProperties(
            this,
            null as String?,
            extensionCount,
            availableExtensions
        )

        return@areExtensionsSupported availableExtensions
            .map(VkExtensionProperties::extensionNameString)
            .containsAll(DEVICE_EXTENSIONS)
    }
}

data class QueueFamilyIndicesIncomplete (
    val graphicsFamily: Int? = null,
    val presentFamily: Int? = null,
) {
    val complete: Boolean
        get() = this.graphicsFamily != null && this.presentFamily != null

    fun intoComplete() = QueueFamilyIndices(
            graphicsFamily!!,
            presentFamily!!,
        )
}

data class QueueFamilyIndices (
    val graphicsFamily: Int,
    val presentFamily: Int,
) {
    fun unique() = listOf(graphicsFamily, presentFamily).distinct()

    fun toArray(): IntArray {
        return intArrayOf(graphicsFamily, presentFamily)
    }
}

fun VkPhysicalDevice.findQueueFamilies(): QueueFamilyIndicesIncomplete {
    var indices = QueueFamilyIndicesIncomplete()

    MemoryStack.stackPush().use { stack ->
        val count = stack.ints(0)

        vkGetPhysicalDeviceQueueFamilyProperties(this, count, null)

        val queueFamilies = VkQueueFamilyProperties.malloc(count[0], stack)

        vkGetPhysicalDeviceQueueFamilyProperties(this, count, queueFamilies)

        val presentSupport = stack.ints(VK_FALSE)

        (0..<count[0]).forEach { index ->
            if (queueFamilies[index].queueFlags() and VK_QUEUE_GRAPHICS_BIT != 0)
                indices = indices.copy(graphicsFamily = index)

            vkGetPhysicalDeviceSurfaceSupportKHR(
                    this@findQueueFamilies,
                    index,
                    Surface,
                    presentSupport)

            if (presentSupport[0] != 0)
                indices = indices.copy(presentFamily = index)

            if (indices.complete) return@use
        }
        throw RuntimeException("No suitable physical devices")
    }

    return indices
}

fun createLogicalDevice() {
    MemoryStack.stackPush().use { stack ->
        val indices = PhysicalDevice.findQueueFamilies().intoComplete()

        val uniqueQueueFamilies = indices.unique()
        val queueCreateInfos = VkDeviceQueueCreateInfo.calloc(uniqueQueueFamilies.size, stack).run {
            uniqueQueueFamilies.zip(this).forEach {
                it.second.apply {
                    sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                    queueFamilyIndex(it.first)
                    pQueuePriorities(stack.floats(1F))
                }
            }

            return@run this@run
        }

        val deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack)

        val createInfo = VkDeviceCreateInfo.calloc(stack).apply {
            sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
            pQueueCreateInfos(queueCreateInfos)
            pEnabledFeatures(deviceFeatures)
            ppEnabledExtensionNames(asPointerBuffer(stack, DEVICE_EXTENSIONS).also {
                println("Extensions:")
                it.iter().forEach {
                    println(it)
                }
            })

            if (VulkanInfo.VALIDATION_LAYERS_ENABLED)
                ppEnabledLayerNames(asPointerBuffer(stack, VALIDATION_LAYERS!!))
        }

        val pDevice = stack.pointers(VK_NULL_HANDLE)

        if (vkCreateDevice(PhysicalDevice, createInfo, null, pDevice) != VK_SUCCESS)
            throw RuntimeException("Failed to create logical device")

        Device = VkDevice(pDevice[0], PhysicalDevice, createInfo)

        val pQueue = stack.pointers(VK_NULL_HANDLE)

        vkGetDeviceQueue(Device, indices.graphicsFamily, 0, pQueue)
        GraphicsQueue = VkQueue(pQueue[0], Device)

        vkGetDeviceQueue(Device, indices.presentFamily, 0, pQueue)
        PresentQueue = VkQueue(pQueue[0], Device)
    }
}