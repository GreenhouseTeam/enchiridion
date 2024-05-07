package dev.greenhouseteam.enchiridion;

import dev.greenhouseteam.enchiridion.platform.EnchiridionPlatformHelperNeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Enchiridion.MOD_ID)
public class EnchiridionNeoForge {

    public EnchiridionNeoForge(IEventBus eventBus) {
        Enchiridion.init(new EnchiridionPlatformHelperNeoForge());
    }
}