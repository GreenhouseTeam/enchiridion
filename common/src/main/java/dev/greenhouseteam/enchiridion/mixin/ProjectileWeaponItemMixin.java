package dev.greenhouseteam.enchiridion.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
    @ModifyReturnValue(method = "draw", at = @At(value = "RETURN", ordinal = 0))
    private static List<ItemStack> enchiridion$allowArrowsWithoutProjectile(List<ItemStack> original, ItemStack stack, ItemStack ammoStack, LivingEntity entity) {
        if (ammoStack.isEmpty()) {
            if (entity.level() instanceof ServerLevel serverLevel) {
                int projectileCount = EnchantmentHelper.processProjectileCount(serverLevel, stack, entity, 1);
                List<ItemStack> stacks = new ArrayList<>(projectileCount);
                LootParams.Builder params = new LootParams.Builder(serverLevel).withParameter(LootContextParams.TOOL, stack);
                if (stack.getEnchantments().entrySet().stream().anyMatch(entry -> entry.getKey().isBound() && entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.ALLOW_FIRING_WITHOUT_PROJECTILE).stream().allMatch(effect -> {
                    params.withParameter(LootContextParams.ENCHANTMENT_LEVEL, entry.getIntValue());
                    return effect.map(condition -> condition.test(new LootContext.Builder(params.create(LootContextParamSets.ENCHANTED_ITEM)).create(Optional.empty()))).orElse(true);
                }))) {
                    for (int i = 0; i < projectileCount; ++i) {
                        ItemStack newAmmoStack = new ItemStack(Items.ARROW);
                        newAmmoStack.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
                        stacks.add(newAmmoStack);
                    }
                }
                return stacks;
            }
        }
        return original;
    }
}
