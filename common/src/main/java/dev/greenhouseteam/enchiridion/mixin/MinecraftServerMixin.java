package dev.greenhouseteam.enchiridion.mixin;

import dev.greenhouseteam.enchiridion.util.TargetUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow public abstract PlayerList getPlayerList();

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tickServer(Ljava/util/function/BooleanSupplier;)V"))
    private void enchiridion$updateLookTargets(CallbackInfo ci) {
        getPlayerList().getPlayers().forEach(TargetUtil::updateBlockLookEffects);
    }
}
