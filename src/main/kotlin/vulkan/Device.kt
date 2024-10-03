package gay.block36.voxel.vulkan

import gay.block36.voxel.util.iter
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR
import org.lwjgl.vulkan.VK10.*


lateinit var PhysicalDevice: VkPhysicalDevice
lateinit var Device: VkDevice
lateinit var GraphicsQueue: VkQueue

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
            .take(1)
            .forEach {
                PhysicalDevice = it
                return@use 
            }

        throw RuntimeException("No suitable device found")
    }
}


data class QueueFamilyIndicesIncomplete (
    val graphicsFamily: Int? = null,
    val presentFamily: Int? = null,
) {
    val complete: Boolean
        get() = this.graphicsFamily != null && this.presentFamily != null

    fun intoComplete() =
        if (this.complete) QueueFamilyIndices(
            graphicsFamily!!,
            presentFamily!!,
        )
        else throw IllegalArgumentException(
            "Incomplete Queue Family Indices attempted to be converted to complete"
        )
}

data class QueueFamilyIndices (
    val graphicsFamily: Int,
    val presentFamily: Int,
) {
    fun unique() = listOf(graphicsFamily, presentFamily).distinct()
}

private fun VkPhysicalDevice.findQueueFamilies(): QueueFamilyIndices {
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

    return indices.intoComplete()
}

fun createLogicalDevice() {
    MemoryStack.stackPush().use { stack ->
        val indices = PhysicalDevice.findQueueFamilies()

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

            if (VulkanInfo.VALIDATION_LAYERS_ENABLED)
                ppEnabledLayerNames(validationLayersAsPointerBuffer(stack))
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