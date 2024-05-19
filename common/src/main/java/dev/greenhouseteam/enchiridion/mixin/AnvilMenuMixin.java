package dev.greenhouseteam.enchiridion.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.enchantment.category.ItemEnchantmentCategories;
import dev.greenhouseteam.enchiridion.registry.EnchiridionDataComponents;
import dev.greenhouseteam.enchiridion.util.EnchiridionUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow @Final private DataSlot cost;

    public AnvilMenuMixin(@Nullable MenuType<?> menuType, int syncId, Inventory inventory, ContainerLevelAccess access) {
        super(menuType, syncId, inventory, access);
    }

    @WrapWithCondition(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V", ordinal = 4))
    private boolean enchiridion$cancelCost(DataSlot instance, int i) {
        return false;
    }

    @WrapWithCondition(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1))
    private <T> boolean enchiridion$cancelCostFromItem(ItemStack instance, DataComponentType<? super T> component, T value) {
        return false;
    }

    @ModifyVariable(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;setEnchantments(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/ItemEnchantments;)V"), ordinal = 1)
    private ItemStack enchiridion$setItemCategories(ItemStack original, @Local(ordinal = 2) ItemStack otherInput) {
        ItemEnchantmentCategories categories = original.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY);
            ItemEnchantmentCategories otherCategories = otherInput.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY);

        otherCategories.getCategories().forEach((category, enchantments) -> enchantments.forEach(enchantment -> {
            if (categories.isValid(category, enchantment, EnchantmentHelperAccessor.enchiridion$invokeGetComponentType(original)))
                categories.addCategoryWithEnchantment(category, enchantment);
        }));

        if (!categories.isEmpty())
            original.set(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, categories);

        return original;
    }

    @ModifyArg(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 4), index = 1)
    private ItemStack enchiridion$dontAllowIncorrectCategories(ItemStack original) {
        ItemEnchantmentCategories categories = original.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY);
        if (EnchantmentHelper.getEnchantmentsForCrafting(original).keySet().stream().anyMatch(enchantment -> EnchiridionUtil.getFirstEnchantmentCategory(Enchiridion.getHelper().getReqistryAccess(), enchantment) != null && !categories.contains(enchantment))) {
            cost.set(0);
            return ItemStack.EMPTY;
        }
        return original;
    }
}
