package dev.greenhouseteam.enchiridion.mixin;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.enchantment.category.EnchantmentCategory;
import dev.greenhouseteam.enchiridion.enchantment.category.ItemEnchantmentCategories;
import dev.greenhouseteam.enchiridion.registry.EnchiridionDataComponents;
import dev.greenhouseteam.enchiridion.util.EnchiridionUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @ModifyVariable(method = "updateEnchantments", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/ItemEnchantments$Mutable;toImmutable()Lnet/minecraft/world/item/enchantment/ItemEnchantments;", shift = At.Shift.BEFORE))
    private static ItemEnchantments.Mutable enchiridion$updateAndValidateCategories(ItemEnchantments.Mutable mutable, ItemStack stack, Consumer<ItemEnchantments.Mutable> consumer) {
        ItemEnchantmentCategories categories = stack.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, new ItemEnchantmentCategories());

        mutable.keySet().forEach(enchantment -> {
            Holder<EnchantmentCategory> category = EnchiridionUtil.getFirstEnchantmentCategory(Enchiridion.getHelper().getReqistryAccess(), enchantment);
            if (category != null && category.isBound() && !EnchiridionUtil.isValidInCategory(category, categories.get(category), enchantment)) {
                categories.removeCategoryWithEnchantment(category, enchantment);
                return;
            }

            if (category == null || !category.isBound() || categories.getCategories().containsKey(enchantment) && categories.getCategories().get(category).contains(enchantment) || !EnchiridionUtil.categoryAcceptsNewEnchantments(category, categories))
                return;
            categories.addCategoryWithEnchantment(category, enchantment);
        });

        mutable.removeIf(enchantment -> EnchiridionUtil.getFirstEnchantmentCategory(Enchiridion.getHelper().getReqistryAccess(), enchantment) != null && !categories.contains(enchantment));
        if (!categories.equals(stack.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY))) {
            if (categories.isEmpty())
                stack.remove(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES);
            else
                stack.applyComponents(DataComponentPatch.builder().set(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, categories).build());
        }

        return mutable;
    }
}
