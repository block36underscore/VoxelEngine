package gay.block36.voxel.vulkan;

import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;

import static org.lwjgl.vulkan.VK10.VK_FALSE;

public class DebugCallback {
    public static int debugCallback(int messageSeverity, int messageType, long pCallbackData, long pUserData) {

        VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);

        System.err.println("Validation layer: " + callbackData.pMessageString());

        return VK_FALSE;
    }
}
