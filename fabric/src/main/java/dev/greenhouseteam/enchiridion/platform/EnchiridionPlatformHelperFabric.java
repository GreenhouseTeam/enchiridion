package dev.greenhouseteam.enchiridion.platform;

import dev.greenhouseteam.enchiridion.EnchiridionFabric;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistryAccess;

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
}
