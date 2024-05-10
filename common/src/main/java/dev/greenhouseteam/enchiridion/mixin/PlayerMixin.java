package dev.greenhouseteam.enchiridion.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.greenhouseteam.enchiridion.enchantment.effects.PreventHungerConsumptionEffect;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSprinting()Z"))
    private boolean enchiridion$wrapExhaustionFromJumping(boolean original) {
        return original && !PreventHungerConsumptionEffect.shouldPreventSprintingConsumption((Player)(Object)this);
    }

    @WrapWithCondition(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"))
    private boolean enchiridion$wrapExhaustionFromJumping(Player instance, float value) {
        return !PreventHungerConsumptionEffect.shouldPreventJumpingConsumption(instance);
    }
}
