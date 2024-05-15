package dev.greenhouseteam.enchiridion.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.access.UpgradeableEnchantmentMenuAccess;
import dev.greenhouseteam.enchiridion.util.EnchantingTableUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends AbstractContainerScreen<EnchantmentMenu> {
    public EnchantmentScreenMixin(EnchantmentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @ModifyExpressionValue(method = "renderBg", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceLevel:I"))
    private int enchiridion$disableIfBookshelfCountIsntValid(int original, @Local(ordinal = 5) int i) {
        if (((UpgradeableEnchantmentMenuAccess)menu).enchiridion$getRequiredBookshelves(i) - ((UpgradeableEnchantmentMenuAccess)menu).enchiridion$getBookshelfCount(i) > 0)
            return Integer.MIN_VALUE;
        return original;
    }

    @ModifyArg(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 2))
    private ResourceLocation enchiridion$renderDisabledUpgradeWidget(ResourceLocation original, @Local(ordinal = 5) int i) {
        if (((UpgradeableEnchantmentMenuAccess)menu).enchiridion$getRequiredBookshelves(i) != -1)
            return Enchiridion.asResource("container/enchanting_table/level_up_disabled");
        return original;
    }

    @ModifyArg(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 5))
    private ResourceLocation enchiridion$renderUpgradeWidget(ResourceLocation original, @Local(ordinal = 5) int i) {
        if (((UpgradeableEnchantmentMenuAccess)menu).enchiridion$getRequiredBookshelves(i) != -1)
            return Enchiridion.asResource("container/enchanting_table/level_up");
        return original;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 2, shift = At.Shift.AFTER))
    private List<Component> enchiridion$addBookshelfRequirementTextLacksLevels(List<Component> original, @Local(ordinal = 3) int i) {
        int requiredBookshelves = ((UpgradeableEnchantmentMenuAccess)menu).enchiridion$getRequiredBookshelves(i);
        int extraBookshelvesRequired = requiredBookshelves - ((UpgradeableEnchantmentMenuAccess)menu).enchiridion$getBookshelfCount(i);
        if (extraBookshelvesRequired > 0)
            original.add(Component.translatable("container.enchiridion.enchant.bookshelf.requirement", requiredBookshelves).withStyle(ChatFormatting.RED));
        return original;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 4, shift = At.Shift.AFTER))
    private List<Component> enchiridion$addBookshelfRequirementText(List<Component> original, @Local(ordinal = 3) int i) {
        int requiredBookshelves = ((UpgradeableEnchantmentMenuAccess)menu).enchiridion$getRequiredBookshelves(i);
        if (requiredBookshelves != -1) {
            int extraBookshelvesRequired = requiredBookshelves - ((UpgradeableEnchantmentMenuAccess)menu).enchiridion$getBookshelfCount(i);
            if (extraBookshelvesRequired > 0) {
                if (extraBookshelvesRequired == 1)
                    original.add(Component.translatable("container.enchiridion.enchant.bookshelves.one_more").withStyle(ChatFormatting.RED));
                else
                    original.add(Component.translatable("container.enchiridion.enchant.bookshelves.many_more", extraBookshelvesRequired).withStyle(ChatFormatting.RED));
            } else {
                if (requiredBookshelves == 1)
                    original.add(Component.translatable("container.enchiridion.enchant.bookshelves.one").withStyle(ChatFormatting.GRAY));
                else
                    original.add(Component.translatable("container.enchiridion.enchant.bookshelves.many", requiredBookshelves).withStyle(ChatFormatting.GRAY));
            }
        }
        return original;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
    private <E> E enchiridion$modifyEnchantMessageToLevelUpMessage(E original, @Local(ordinal = 3) int i) {
        if (((UpgradeableEnchantmentMenuAccess)menu).enchiridion$getRequiredBookshelves(i) != -1) {
            Holder<Enchantment> enchantment = minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap().byId(menu.enchantClue[i]);
            if (enchantment != null && enchantment.isBound())
                return (E) Component.translatable("container.enchiridion.enchant.level_up", Enchantment.getFullname(enchantment, menu.levelClue[i] - 1), Enchantment.getFullname(enchantment, menu.levelClue[i]));
        }
        return original;
    }

    @ModifyVariable(method = "render", at = @At(value = "LOAD", ordinal = 0), ordinal = 6)
    private int enchiridion$modifyLapisCountForLevelling(int original, @Local(ordinal = 3) int i) {
        if (((UpgradeableEnchantmentMenuAccess)menu).enchiridion$getRequiredBookshelves(i) != -1) {
            Holder<Enchantment> enchantment = minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap().byId(menu.enchantClue[i]);
            if (enchantment != null && enchantment.isBound())
                return EnchantingTableUtil.getLapisCountForLevelling(menu.levelClue[i], enchantment.value().getMaxLevel());
        }
        return original;
    }
}
