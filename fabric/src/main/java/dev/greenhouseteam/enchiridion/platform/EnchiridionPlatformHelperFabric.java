package dev.greenhouseteam.enchiridion.platform;

import dev.greenhouseteam.enchiridion.EnchiridionFabric;
import dev.greenhouseteam.enchiridion.mixin.fabric.client.MinecraftAccessor;
import dev.greenhouseteam.enchiridion.network.clientbound.SyncEnchantedFrozenStateClientboundPacket;
import dev.greenhouseteam.enchiridion.registry.EnchiridionAttachments;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityLookup;

public class EnchiridionPlatformHelperFabric implements EnchiridionPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public RegistryAccess getReqistryAccess() {
        return EnchiridionFabric.getRegistryAccess();
    }

    @Override
    public boolean isLoaderResourcePack(Resource resource) {
        return resource.source().location().title().contains(Component.translatable("pack.name.fabricMod"));
    }

    @Override
    public void setFrozenByEnchantment(Entity entity, boolean value) {
        if (!value)
            entity.removeAttached(EnchiridionAttachments.FROZEN_BY_ENCHANTMENT);
        else
            entity.setAttached(EnchiridionAttachments.FROZEN_BY_ENCHANTMENT, true);
        if (!entity.level().isClientSide()) {
            for (ServerPlayer player : PlayerLookup.tracking(entity))
                ServerPlayNetworking.send(player, new SyncEnchantedFrozenStateClientboundPacket(entity.getId(), value));
            if (entity instanceof ServerPlayer player)
                ServerPlayNetworking.send(player, new SyncEnchantedFrozenStateClientboundPacket(entity.getId(), value));
        }
    }

    @Override
    public boolean isFrozenByEnchantment(Entity entity) {
        return entity.getAttachedOrElse(EnchiridionAttachments.FROZEN_BY_ENCHANTMENT, false);
    }

    @Override
    public boolean isClientThread() {
        return Thread.currentThread() == ((MinecraftAccessor) Minecraft.getInstance()).enchiridion$getGameThread();
    }
}
