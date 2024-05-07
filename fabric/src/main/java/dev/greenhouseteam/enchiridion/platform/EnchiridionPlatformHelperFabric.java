package dev.greenhouseteam.enchiridion.platform;

import net.fabricmc.loader.api.FabricLoader;

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
}
