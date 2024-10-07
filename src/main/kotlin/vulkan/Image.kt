package gay.block36.voxel.vulkan

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkImageViewCreateInfo

lateinit var SwapChainImageViews: List<Long>

fun createImageViews() {
    SwapChainImageViews = ArrayList(SwapChainImages.size)

    MemoryStack.stackPush().use { stack ->
        val pImageView = stack.mallocLong(1)
        SwapChainImages.forEach { image ->
            val createInfo = VkImageViewCreateInfo.calloc(stack).apply {
                sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                image(image)
                viewType(VK_IMAGE_VIEW_TYPE_2D)
                format(SwapChainImageFormat)

                components().r(VK_COMPONENT_SWIZZLE_IDENTITY)
                components().g(VK_COMPONENT_SWIZZLE_IDENTITY)
                components().b(VK_COMPONENT_SWIZZLE_IDENTITY)
                components().a(VK_COMPONENT_SWIZZLE_IDENTITY)

                subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                subresourceRange().baseMipLevel(0)
                subresourceRange().levelCount(1)
                subresourceRange().baseArrayLayer(0)
                subresourceRange().layerCount(1)
            }

            if (vkCreateImageView(Device, createInfo, null, pImageView) != VK_SUCCESS)
                throw RuntimeException("Failed to create image views")

            SwapChainImageViews += pImageView[0]
        }
    }
}