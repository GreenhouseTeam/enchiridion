package dev.greenhouseteam.enchiridion.client;

import dev.greenhouseteam.enchiridion.client.util.EnchiridionModelUtil;
import dev.greenhouseteam.enchiridion.network.clientbound.SetOutlinedBlocksClientboundPacket;
import dev.greenhouseteam.enchiridion.network.clientbound.SyncEnchantScrollIndexClientboundPacket;
import dev.greenhouseteam.enchiridion.network.clientbound.SyncEnchantedFrozenStateClientboundPacket;
import dev.greenhouseteam.enchiridion.network.clientbound.SyncEnchantmentLevelUpSeedsPacket;
import dev.greenhouseteam.enchiridion.util.TooltipUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import java.util.concurrent.CompletableFuture;

public class EnchiridionFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerPackets();

        PreparableModelLoadingPlugin.register((resourceManager, executor) ->
                CompletableFuture.completedFuture(EnchiridionModelUtil.getEnchiridionModels(resourceManager)),
                (data, pluginContext) -> {
                    pluginContext.addModels(data);
                    pluginContext.resolveModel().register(context -> EnchiridionModelUtil.getVariantModel(context.id(), context::getOrLoadModel));
                });

        ItemTooltipCallback.EVENT.register(TooltipUtil::modifyEnchantmentTooltips);

        WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
            GlowingBlocksClientHolder.render(context.gameRenderer(), context.camera(), context.matrixStack(), context.positionMatrix(), context.projectionMatrix(), context.consumers());
        });
        ClientTickEvents.END_WORLD_TICK.register(GlowingBlocksClientHolder::tick);
    }

    public static void registerPackets() {
        ClientPlayNetworking.registerGlobalReceiver(SyncEnchantScrollIndexClientboundPacket.TYPE, (payload, context) -> payload.handle());
        ClientPlayNetworking.registerGlobalReceiver(SyncEnchantedFrozenStateClientboundPacket.TYPE, (payload, context) -> payload.handle());
        ClientPlayNetworking.registerGlobalReceiver(SyncEnchantmentLevelUpSeedsPacket.TYPE, (payload, context) -> payload.handle());
        ClientPlayNetworking.registerGlobalReceiver(SetOutlinedBlocksClientboundPacket.TYPE, (payload, context) -> payload.handle());
    }
}
