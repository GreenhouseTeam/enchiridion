package dev.greenhouseteam.enchiridion.platform;

import net.minecraft.core.RegistryAccess;

public interface EnchiridionPlatformHelper {

    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    RegistryAccess getReqistryAccess();
}