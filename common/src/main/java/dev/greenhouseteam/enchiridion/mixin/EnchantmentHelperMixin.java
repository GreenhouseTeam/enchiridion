package dev.greenhouseteam.enchiridion.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.enchantment.category.EnchantmentCategory;
import dev.greenhouseteam.enchiridion.enchantment.category.ItemEnchantmentCategories;
import dev.greenhouseteam.enchiridion.registry.EnchiridionDataComponents;
import dev.greenhouseteam.enchiridion.util.EnchiridionUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
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
            else
                stack.applyComponents(DataComponentPatch.builder().set(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, categories).build());
        }

        return enchantments;
    }
}
