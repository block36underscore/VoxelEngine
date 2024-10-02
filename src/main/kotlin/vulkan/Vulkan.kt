package gay.block36.voxel.vulkan

import org.lwjgl.system.Configuration.DEBUG


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
    pickPhysicalDevice()
}
