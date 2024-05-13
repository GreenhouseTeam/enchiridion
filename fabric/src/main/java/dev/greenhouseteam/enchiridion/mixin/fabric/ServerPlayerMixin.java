package dev.greenhouseteam.enchiridion.mixin.fabric;

import dev.greenhouseteam.enchiridion.util.TargetUtil;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void enchiridion$modifyHeadValue(CallbackInfo ci) {
        TargetUtil.updateBlockLookEffects((ServerPlayer)(Object)this);
    }
}
