package dev.greenhouseteam.enchiridion.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.greenhouseteam.enchiridion.enchantment.effects.PreventHungerConsumptionEffect;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @WrapWithCondition(method = "checkMovementStatistics", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V", ordinal = 3))
    private boolean enchiridion$wrapExhaustionFromSprinting(ServerPlayer instance, float value) {
        return !PreventHungerConsumptionEffect.shouldPreventSprintingConsumption(instance);
    }
}
