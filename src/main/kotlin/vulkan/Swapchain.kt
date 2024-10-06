package gay.block36.voxel.vulkan

import gay.block36.voxel.WindowInfo
import gay.block36.voxel.util.UINT32_MAX
import gay.block36.voxel.util.iter
import org.joml.Math.clamp
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.KHRSurface.*
import org.lwjgl.vulkan.KHRSwapchain.*
import org.lwjgl.vulkan.VK10.*
import java.nio.IntBuffer
import kotlin.properties.Delegates

var SwapChain: Long by Delegates.notNull()
lateinit var SwapChainImages: MutableList<Long>
var SwapChainImageFormat: Int by Delegates.notNull()
lateinit var SwapChainExtent: VkExtent2D

data class SwapChainSupportDetailsIncomplete (
    var capabilities: VkSurfaceCapabilitiesKHR? = null,
    var formats: VkSurfaceFormatKHR.Buffer? = null,
    var presentModes: IntBuffer? = null,
) {

    val complete: Boolean
        get() = capabilities != null
                && formats != null
                && presentModes != null

    fun intoComplete() = SwapChainSupportDetails(
        capabilities!!,
        formats!!,
        presentModes!!,
    )
}

data class SwapChainSupportDetails (
    var capabilities: VkSurfaceCapabilitiesKHR,
    var formats: VkSurfaceFormatKHR.Buffer,
    var presentModes: IntBuffer,
)

fun VkPhysicalDevice.querySwapChainSupport(stack: MemoryStack): SwapChainSupportDetails {
    val details = SwapChainSupportDetailsIncomplete().apply {
        capabilities = VkSurfaceCapabilitiesKHR.malloc(stack)
    }

    vkGetPhysicalDeviceSurfaceCapabilitiesKHR(this, Surface, details.capabilities!!)

    val count = stack.ints(0)

    vkGetPhysicalDeviceSurfaceFormatsKHR(this, Surface, count, null)

    if (count[0] != 0) {
        details.formats = VkSurfaceFormatKHR.malloc(count[0], stack)
        vkGetPhysicalDeviceSurfaceFormatsKHR(this, Surface, count, details.formats)
    }

    vkGetPhysicalDeviceSurfacePresentModesKHR(this, Surface, count, null)

    if (count[0] != 0) {
        details.presentModes = stack.mallocInt(count[0])
        vkGetPhysicalDeviceSurfacePresentModesKHR(this, Surface, count, details.presentModes)
    }

    return details.intoComplete()
}

fun chooseSwapSurfaceFormat(availableFormats: VkSurfaceFormatKHR.Buffer): VkSurfaceFormatKHR {
    availableFormats.forEach {
        if (it.format() == VK_FORMAT_B8G8R8A8_SRGB && it.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
            return@chooseSwapSurfaceFormat it
    }

    return availableFormats[0]
}

fun chooseSwapPresentMode(availablePresentModes: IntBuffer): Int {
    availablePresentModes.iter().forEach {
        if (it == VK_PRESENT_MODE_MAILBOX_KHR)
            return@chooseSwapPresentMode it
    }

    return VK_PRESENT_MODE_FIFO_KHR
}

fun chooseSwapExtent(stack: MemoryStack, capabilities: VkSurfaceCapabilitiesKHR): VkExtent2D {
    if (capabilities.currentExtent().width() != UINT32_MAX) {
        return capabilities.currentExtent()
    }

    val actualExtent = VkExtent2D.malloc(stack).set(WindowInfo.WIDTH, WindowInfo.HEIGHT)

    val minExtent = capabilities.minImageExtent()
    val maxExtent = capabilities.maxImageExtent()

    actualExtent.width(clamp(minExtent.width(), maxExtent.width(), actualExtent.width()))
    actualExtent.height(clamp(minExtent.height(), maxExtent.height(), actualExtent.height()))

    return actualExtent
}

fun createSwapChain() {
    stackPush().use { stack ->
        val swapChainSupport: SwapChainSupportDetails = PhysicalDevice.querySwapChainSupport(stack)
        val surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.formats)
        val presentMode = chooseSwapPresentMode(swapChainSupport.presentModes)
        val extent = chooseSwapExtent(stack, swapChainSupport.capabilities)

        val imageCount = stack.ints(swapChainSupport.capabilities.minImageCount() + 1)

        if (swapChainSupport.capabilities.maxImageCount() > 0 && imageCount[0] > swapChainSupport.capabilities.maxImageCount()) {
            imageCount.put(0, swapChainSupport.capabilities.maxImageCount())
        }

        val createInfo = VkSwapchainCreateInfoKHR.calloc(stack).apply {
            sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
            surface(Surface)

            // Image settings
            minImageCount(imageCount[0])
            imageFormat(surfaceFormat.format())
            imageColorSpace(surfaceFormat.colorSpace())
            imageExtent(extent)
            imageArrayLayers(1)
            imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)

            val indices: QueueFamilyIndices = PhysicalDevice.findQueueFamilies().intoComplete()

            if (indices.graphicsFamily != indices.presentFamily) {
                imageSharingMode(VK_SHARING_MODE_CONCURRENT)
                pQueueFamilyIndices(stack.ints(indices.graphicsFamily, indices.presentFamily))
            } else {
                imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
            }

            preTransform(swapChainSupport.capabilities.currentTransform())
            compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
            presentMode(presentMode)
            clipped(true)

            oldSwapchain(VK_NULL_HANDLE)
        }

        val pSwapChain = stack.longs(VK_NULL_HANDLE)

        if (vkCreateSwapchainKHR(Device, createInfo, null, pSwapChain) != VK_SUCCESS) {
            throw RuntimeException("Failed to create swap chain")
        }

        SwapChain = pSwapChain[0]

        vkGetSwapchainImagesKHR(Device, SwapChain, imageCount, null)

        val pSwapchainImages = stack.mallocLong(imageCount[0])

        vkGetSwapchainImagesKHR(Device, SwapChain, imageCount, pSwapchainImages)

        SwapChainImages = arrayListOf(imageCount[0].toLong())

        for (i in 0 until pSwapchainImages.capacity()) {
            SwapChainImages.add(pSwapchainImages[i])
        }

        SwapChainImageFormat = surfaceFormat.format()
        SwapChainExtent = VkExtent2D.create().set(extent)
    }
}