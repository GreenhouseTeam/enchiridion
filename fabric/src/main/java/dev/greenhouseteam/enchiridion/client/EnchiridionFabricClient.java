package dev.greenhouseteam.enchiridion.client;

import dev.greenhouseteam.enchiridion.client.util.EnchiridionModelUtil;
import dev.greenhouseteam.enchiridion.util.TooltipUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;

import java.util.concurrent.CompletableFuture;

public class EnchiridionFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PreparableModelLoadingPlugin.register((resourceManager, executor) ->
                CompletableFuture.completedFuture(EnchiridionModelUtil.getEnchiridionModels(resourceManager)),
                (data, pluginContext) -> {
                    pluginContext.addModels(data);
                    pluginContext.resolveModel().register(context -> EnchiridionModelUtil.getVariantModel(context.id(), context::getOrLoadModel));
                });

        ItemTooltipCallback.EVENT.register(TooltipUtil::modifyEnchantmentTooltips);
    }
}
