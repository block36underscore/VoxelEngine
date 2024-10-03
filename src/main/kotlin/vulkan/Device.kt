package gay.block36.voxel.vulkan

import gay.block36.voxel.Instance
import gay.block36.voxel.util.iter
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
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

fun createLogicalDevice() {
    MemoryStack.stackPush().use { stack ->
        val indicies = PhysicalDevice.findQueueFamilies()
        require(indicies.graphicsFamily != null)

        val queueCreateInfos = VkDeviceQueueCreateInfo.calloc(1, stack).run {
            sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
            queueFamilyIndex(indicies.graphicsFamily)
            pQueuePriorities(stack.floats(1F))
            return@run this
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

        val pGraphicsQueue = stack.pointers(VK_NULL_HANDLE)

        vkGetDeviceQueue(Device, indicies.graphicsFamily, 0, pGraphicsQueue)

        GraphicsQueue = VkQueue(pGraphicsQueue[0], Device)
    }
}