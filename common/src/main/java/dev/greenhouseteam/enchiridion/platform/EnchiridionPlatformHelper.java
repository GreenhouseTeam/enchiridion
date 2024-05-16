package dev.greenhouseteam.enchiridion.platform;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.Set;

public interface EnchiridionPlatformHelper {

    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }

    RegistryAccess getReqistryAccess();

    boolean isLoaderResourcePack(Resource resource);

    void sendClientbound(ServerPlayer player, CustomPacketPayload payload);

    void sendServerbound(CustomPacketPayload payload);

    void setFrozenByEnchantment(Entity entity, boolean value);

    boolean isFrozenByEnchantment(Entity entity);

    boolean isClientThread();
}