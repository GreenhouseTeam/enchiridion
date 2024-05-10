package dev.greenhouseteam.enchiridion.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;

public class ClientRegistryAccessReference {
    public static RegistryAccess get() {
        return Minecraft.getInstance().getConnection().registryAccess();
    }
}
