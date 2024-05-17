package dev.greenhouseteam.enchiridion.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(BowItem.class)
public class BowItemMixin {
    @ModifyExpressionValue(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private boolean enchiridion$allowReleasingBowWithEmpty(boolean original, ItemStack stack, Level level, LivingEntity entity, int bl) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            LootParams.Builder params = new LootParams.Builder(serverLevel).withParameter(LootContextParams.TOOL, stack);
            return stack.getEnchantments().entrySet().stream().noneMatch(entry -> entry.getKey().isBound() && (entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.ALLOW_FIRING_WITHOUT_PROJECTILE).isEmpty() || entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.ALLOW_FIRING_WITHOUT_PROJECTILE).stream().allMatch(effect -> {
                params.withParameter(LootContextParams.ENCHANTMENT_LEVEL, entry.getIntValue());
                return effect.test(new LootContext.Builder(params.create(LootContextParamSets.ENCHANTED_ITEM)).create(Optional.empty()));
            })));
        }
        return original;
    }

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z"))
    private boolean enchiridion$allowShootingBowWithEmpty(boolean original, Level level, Player player, InteractionHand hand, @Local ItemStack stack) {
        if (level instanceof ServerLevel serverLevel) {
            LootParams.Builder params = new LootParams.Builder(serverLevel).withParameter(LootContextParams.TOOL, stack);
            return stack.getEnchantments().entrySet().stream().anyMatch(entry -> entry.getKey().isBound() && (entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.ALLOW_FIRING_WITHOUT_PROJECTILE).isEmpty() || entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.ALLOW_FIRING_WITHOUT_PROJECTILE).stream().allMatch(effect -> {
                params.withParameter(LootContextParams.ENCHANTMENT_LEVEL, entry.getIntValue());
                return effect.test(new LootContext.Builder(params.create(LootContextParamSets.ENCHANTED_ITEM)).create(Optional.empty()));
            })));
        }
        return original;
    }
}
