package gay.block36.voxel.vulkan

import gay.block36.voxel.Instance
import gay.block36.voxel.util.iter
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkPhysicalDevice
import org.lwjgl.vulkan.VkQueueFamilyProperties

lateinit var PhysicalDevice: VkPhysicalDevice

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
            }.filter(VkPhysicalDevice::isSuitable)
            .take(1)
            .forEach {
                PhysicalDevice = it
                return@use 
            }

        throw RuntimeException("No suitable device found")
    }
}

private fun VkPhysicalDevice.isSuitable() = this.findQueueFamilies().graphicsFamily != null

@JvmInline
private value class QueueFamilyIndices(val graphicsFamily: Int? = null)

private fun VkPhysicalDevice.findQueueFamilies(): QueueFamilyIndices {
    var indicies = QueueFamilyIndices()

    MemoryStack.stackPush().use { stack ->
        val count = stack.ints(0)

        vkGetPhysicalDeviceQueueFamilyProperties(this, count, null)

        val queueFamilies = VkQueueFamilyProperties.malloc(count[0], stack)

        vkGetPhysicalDeviceQueueFamilyProperties(this, count, queueFamilies)

        (0..<count[0]).asSequence().filter {
            queueFamilies[it].queueFlags() and VK_QUEUE_GRAPHICS_BIT != 0
        }.take(1)
            .firstOrNull()
            ?.let {
                indicies = QueueFamilyIndices(it)
            }
    }

    return indicies
}
