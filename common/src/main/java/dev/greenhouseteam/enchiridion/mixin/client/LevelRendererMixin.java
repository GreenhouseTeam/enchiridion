package dev.greenhouseteam.enchiridion.mixin.client;

import dev.greenhouseteam.enchiridion.client.GlowingBlocksClientHolder;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(LevelRenderer.class)
public class LevelRendererMixin {


    // This is probably pretty fragile, but it works:tm:
    // Thank fabric WorldRenderer mixin for having a similar injection point that lead me to this
    @ModifyVariable(method = "renderLevel",
            at = @At(value = "CONSTANT",
                    args="stringValue=blockentities",
                    ordinal = 0),
            ordinal=3
    )
    private boolean modify(boolean value) {
        // Changes the flag that makes the glowing render source actually work.
        // Normally the flag is only true if there are glowing entities.
        return value || GlowingBlocksClientHolder.isGlowing();
    }


}
