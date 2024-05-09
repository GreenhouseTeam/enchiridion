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
    @ModifyVariable(method = "updateEnchantments", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/ItemEnchantments$Mutable;toImmutable()Lnet/minecraft/world/item/enchantment/ItemEnchantments;"))
    private static ItemEnchantments.Mutable enchiridion$updateCategories(ItemEnchantments.Mutable mutable, ItemStack stack, Consumer<ItemEnchantments.Mutable> consumer) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        ItemEnchantmentCategories categories = stack.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, new ItemEnchantmentCategories());

        mutable.keySet().forEach(holder -> {
            Holder<EnchantmentCategory> category = EnchiridionUtil.getFirstEnchantmentCategory(Enchiridion.getHelper().getReqistryAccess(), holder);
            if (category == null || !category.isBound() || categories.getCategories().containsKey(holder) && categories.getCategories().get(category).contains(holder))
                return;
            categories.addCategoryWithEnchantment(category, holder);
        });

        mutable.removeIf(enchantmentHolder -> !EnchiridionUtil.isValidInCategory(Enchiridion.getHelper().getReqistryAccess(), categories, enchantmentHolder));
        builder.set(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, categories);
        stack.applyComponents(builder.build());

        return mutable;
    }
}
