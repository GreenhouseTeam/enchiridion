package dev.greenhouseteam.enchiridion.util;

import dev.greenhouseteam.enchiridion.Enchiridion;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;

public class ClientRegistryAccessReference {
    public static RegistryAccess get(MinecraftServer server) {
        if (server == null || Enchiridion.getHelper().isClientThread())
            return Minecraft.getInstance().level.registryAccess();
        return server.registryAccess();
    }
}
