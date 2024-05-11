package dev.greenhouseteam.enchiridion.platform;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.Entity;

public interface EnchiridionPlatformHelper {

    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    RegistryAccess getReqistryAccess();

    boolean isLoaderResourcePack(Resource resource);

    void setFrozenByEnchantment(Entity entity, boolean value);

    boolean isFrozenByEnchantment(Entity entity);

    boolean isClientThread();
}