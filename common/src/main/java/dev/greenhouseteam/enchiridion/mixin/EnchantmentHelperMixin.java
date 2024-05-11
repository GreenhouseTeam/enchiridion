package dev.greenhouseteam.enchiridion.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.enchantment.category.EnchantmentCategory;
import dev.greenhouseteam.enchiridion.enchantment.category.ItemEnchantmentCategories;
import dev.greenhouseteam.enchiridion.registry.EnchiridionDataComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionLootContextParamSets;
import dev.greenhouseteam.enchiridion.registry.EnchiridionLootContextParams;
import dev.greenhouseteam.enchiridion.util.EnchiridionUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @Shadow
    protected static void runIterationOnEquipment(LivingEntity $$0, EnchantmentHelper.EnchantmentInSlotVisitor $$1) {
    }

    @ModifyReturnValue(method = "updateEnchantments", at = @At("RETURN"))
    private static ItemEnchantments enchiridion$updateAndValidateCategories(ItemEnchantments enchantments, ItemStack stack, Consumer<ItemEnchantments.Mutable> consumer, @Local DataComponentType<ItemEnchantments> componentType) {
        ItemEnchantmentCategories categories = stack.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, new ItemEnchantmentCategories());

        enchantments.keySet().forEach(enchantment -> {
            Holder<EnchantmentCategory> category = EnchiridionUtil.getFirstEnchantmentCategory(Enchiridion.getHelper().getReqistryAccess(), enchantment);
            if (category != null && category.isBound() && !EnchiridionUtil.isValidInCategory(category, categories.get(category), enchantment)) {
                categories.removeCategoryWithEnchantment(category, enchantment);
                return;
            }

            if (category == null || !category.isBound() || categories.getCategories().containsKey(enchantment) && categories.getCategories().get(category).contains(enchantment) || !EnchiridionUtil.categoryAcceptsNewEnchantments(category, categories))
                return;
            categories.addCategoryWithEnchantment(category, enchantment);
        });

        if (enchantments.keySet().stream().anyMatch(enchantment -> EnchiridionUtil.getFirstEnchantmentCategory(Enchiridion.getHelper().getReqistryAccess(), enchantment) != null && !categories.contains(enchantment))) {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchantments);
            mutable.removeIf(enchantment -> EnchiridionUtil.getFirstEnchantmentCategory(Enchiridion.getHelper().getReqistryAccess(), enchantment) != null && !categories.contains(enchantment));
            stack.set(componentType, mutable.toImmutable());
        }
        if (!categories.equals(stack.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY))) {
            if (categories.isEmpty())
                stack.remove(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES);
            else {
                DataComponentPatch.Builder builder = DataComponentPatch.builder();
                builder.set(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, categories);
                stack.applyComponents(builder.build());
            }
        }

        return enchantments;
    }

    @ModifyVariable(method = "getDamageProtection", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;runIterationOnEquipment(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V", shift = At.Shift.AFTER))
    private static MutableFloat enchiridion$addVehicleProtection(MutableFloat original, ServerLevel level, LivingEntity entity, DamageSource source) {
        if (!(entity.getFirstPassenger() instanceof LivingEntity passenger))
            return original;
        runIterationOnEquipment(passenger, (enchantment, l, enchantedItem) -> {
            LootParams.Builder params = new LootParams.Builder(level);
            params.withParameter(LootContextParams.THIS_ENTITY, entity);
            params.withParameter(LootContextParams.ENCHANTMENT_LEVEL, l);
            params.withParameter(LootContextParams.ORIGIN, entity.position());
            params.withParameter(LootContextParams.DAMAGE_SOURCE, source);
            params.withParameter(EnchiridionLootContextParams.FIRST_PASSENGER, passenger);
            params.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity());
            params.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity());

            LootContext lootContext = new LootContext.Builder(params.create(EnchiridionLootContextParamSets.VEHICLE_ENCHANTED_DAMAGE)).create(Optional.empty());

            for (ConditionalEffect<EnchantmentValueEffect> conditionalEffect : enchantment.value().getEffects(EnchiridionEnchantmentEffectComponents.VEHICLE_DAMAGE_PROTECTION)) {
                if (conditionalEffect.matches(lootContext)) {
                    original.setValue(conditionalEffect.effect().process(l, entity.getRandom(), original.floatValue()));
                }
            }
        });
        return original;
    }
}
