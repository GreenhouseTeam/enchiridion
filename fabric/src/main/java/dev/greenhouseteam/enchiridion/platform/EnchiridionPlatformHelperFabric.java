package dev.greenhouseteam.enchiridion.platform;

import dev.greenhouseteam.enchiridion.EnchiridionFabric;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.Resource;

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
}
