package dev.greenhouseteam.enchiridion;

import dev.greenhouseteam.enchiridion.platform.EnchiridionPlatformHelperFabric;
import net.fabricmc.api.ModInitializer;

public class EnchiridionFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Enchiridion.init(new EnchiridionPlatformHelperFabric());
    }
}
