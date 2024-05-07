package dev.greenhouseteam.enchiridion;

import dev.greenhouseteam.enchiridion.platform.EnchiridionPlatformHelperFabric;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;

public class EnchiridionFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Enchiridion.init(new EnchiridionPlatformHelperFabric());
        EnchiridionEnchantmentEffectComponents.registerAll(Registry::register);
    }
}
