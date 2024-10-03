package gay.block36.voxel.vulkan

import gay.block36.voxel.Window
import org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10.VK_NULL_HANDLE
import org.lwjgl.vulkan.VK10.VK_SUCCESS
import org.lwjgl.vulkan.VkQueue
import kotlin.properties.Delegates

var Surface: Long by Delegates.notNull()
lateinit var PresentQueue: VkQueue

fun createSurface() {
    MemoryStack.stackPush().use { stack ->
        val pSurface = stack.longs(VK_NULL_HANDLE)

        if (glfwCreateWindowSurface(Instance, Window, null, pSurface) != VK_SUCCESS)
            throw RuntimeException("Failed to create window surface")

        Surface = pSurface[0]
    }
}