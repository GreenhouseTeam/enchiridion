package dev.greenhouseteam.enchiridion.platform;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.EnchiridionFabric;
import dev.greenhouseteam.enchiridion.mixin.fabric.client.MinecraftAccessor;
import dev.greenhouseteam.enchiridion.network.clientbound.SyncEnchantedFrozenStateClientboundPacket;
import dev.greenhouseteam.enchiridion.network.clientbound.SyncEnchantmentLevelUpSeedsPacket;
import dev.greenhouseteam.enchiridion.registry.EnchiridionAttachments;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
    public Collection<ServerPlayer> getTracking(Entity entity) {
        return PlayerLookup.tracking(entity);
    }

    @Override
    public void sendClientbound(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendServerbound(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
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
    public boolean containsEnchantmentSeed(Player player, int index) {
        return player.getAttachedOrElse(EnchiridionAttachments.ENCHANTMENT_LEVEL_UP_SEEDS, List.of()).size() < index;
    }

    @Override
    public int getEnchantmentSeed(Player player, int index) {
        return Optional.ofNullable(player.getAttached(EnchiridionAttachments.ENCHANTMENT_LEVEL_UP_SEEDS)).map(integers -> integers.get(index)).orElse(-1);
    }

    @Override
    public void addEnchantmentSeed(Player player, int index, int seed) {
        player.getAttachedOrCreate(EnchiridionAttachments.ENCHANTMENT_LEVEL_UP_SEEDS, () -> new ArrayList<>()).add(index, seed);

        if (!player.level().isClientSide()) {
            for (ServerPlayer otherPlayer : PlayerLookup.tracking(player))
                ServerPlayNetworking.send(otherPlayer, SyncEnchantmentLevelUpSeedsPacket.add(player.getId(), index, seed));
            if (player instanceof ServerPlayer serverPlayer)
                ServerPlayNetworking.send(serverPlayer, SyncEnchantmentLevelUpSeedsPacket.add(player.getId(), index, seed));
        }
    }

    @Override
    public void clearEnchantmentSeeds(Player player) {
        player.removeAttached(EnchiridionAttachments.ENCHANTMENT_LEVEL_UP_SEEDS);

        if (!player.level().isClientSide()) {
            for (ServerPlayer otherPlayer : PlayerLookup.tracking(player))
                ServerPlayNetworking.send(otherPlayer, SyncEnchantmentLevelUpSeedsPacket.clear(player.getId()));
            if (player instanceof ServerPlayer serverPlayer)
                ServerPlayNetworking.send(serverPlayer, SyncEnchantmentLevelUpSeedsPacket.clear(player.getId()));
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
