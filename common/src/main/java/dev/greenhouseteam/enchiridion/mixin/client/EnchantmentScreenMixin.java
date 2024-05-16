package dev.greenhouseteam.enchiridion.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.access.LevelUpEnchantmentMenuAccess;
import dev.greenhouseteam.enchiridion.client.util.EnchantingTableScreenUtil;
import dev.greenhouseteam.enchiridion.enchantment.category.ItemEnchantmentCategories;
import dev.greenhouseteam.enchiridion.registry.EnchiridionDataComponents;
import dev.greenhouseteam.enchiridion.util.EnchantingTableUtil;
import dev.greenhouseteam.enchiridion.util.EnchiridionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends AbstractContainerScreen<EnchantmentMenu> {
    @Shadow @Final private static ResourceLocation ENCHANTMENT_SLOT_SPRITE;

    public EnchantmentScreenMixin(EnchantmentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @ModifyExpressionValue(method = "renderBg", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceLevel:I"))
    private int enchiridion$disableIfBookshelfCountIsntValid(int original, @Local(ordinal = 5) int i) {
        if (((LevelUpEnchantmentMenuAccess)menu).enchiridion$getRequiredBookshelves(i) - ((LevelUpEnchantmentMenuAccess)menu).enchiridion$getBookshelfCount() > 0)
            return Integer.MIN_VALUE;
        return original;
    }

    @ModifyArg(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 2))
    private ResourceLocation enchiridion$renderDisabledLevelUpWidget(ResourceLocation original, @Local(ordinal = 5) int i) {
        if (((LevelUpEnchantmentMenuAccess)menu).enchiridion$getRequiredBookshelves(i) != -1)
            return Enchiridion.asResource("container/enchanting_table/level_up_disabled");
        return original;
    }

    @ModifyArg(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 5))
    private ResourceLocation enchiridion$renderLevelUpWidget(ResourceLocation original, @Local(ordinal = 5) int i) {
        if (((LevelUpEnchantmentMenuAccess)menu).enchiridion$getRequiredBookshelves(i) != -1)
            return Enchiridion.asResource("container/enchanting_table/level_up");
        return original;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 2, shift = At.Shift.AFTER))
    private List<Component> enchiridion$addBookshelfRequirementTextLacksLevels(List<Component> original, @Local(ordinal = 3) int i) {
        int requiredBookshelves = ((LevelUpEnchantmentMenuAccess)menu).enchiridion$getRequiredBookshelves(i);
        int extraBookshelvesRequired = requiredBookshelves - ((LevelUpEnchantmentMenuAccess)menu).enchiridion$getBookshelfCount();
        if (extraBookshelvesRequired > 0)
            original.add(Component.translatable("container.enchiridion.enchant.bookshelf.requirement", requiredBookshelves).withStyle(ChatFormatting.RED));
        return original;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 4, shift = At.Shift.AFTER))
    private List<Component> enchiridion$addBookshelfRequirementText(List<Component> original, @Local(ordinal = 3) int i) {
        int requiredBookshelves = ((LevelUpEnchantmentMenuAccess)menu).enchiridion$getRequiredBookshelves(i);
        if (requiredBookshelves != -1) {
            int extraBookshelvesRequired = requiredBookshelves - ((LevelUpEnchantmentMenuAccess)menu).enchiridion$getBookshelfCount();
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

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 0), index = 1)
    private Object[] enchiridion$modifyEnchantingTableClueColour(Object[] original, @Local(ordinal = 3) int i) {
        Holder<Enchantment> enchantment = minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap().byId(menu.enchantClue[i]);
        if (enchantment != null && enchantment.isBound()) {
            ItemStack stack = this.menu.getSlot(0).getItem();
            Optional<Integer> color = Optional.ofNullable(EnchiridionUtil.lookupFirstEnchantmentCategory(minecraft.level.registryAccess(), enchantment, stack.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY))).flatMap(holder -> {
                if (!holder.isBound())
                    return Optional.empty();
                return Optional.of(holder.value().color().getValue());
            });
            if (color.isPresent())
                original[0] = color.map(integer -> ((MutableComponent)original[0]).withColor(integer)).get();
        }
        return original;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
    private <E> E enchiridion$modifyEnchantMessageToLevelUpMessage(E original, @Local(ordinal = 3) int i) {
        if (((LevelUpEnchantmentMenuAccess)menu).enchiridion$getRequiredBookshelves(i) != -1) {
            Holder<Enchantment> enchantment = minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap().byId(menu.enchantClue[i]);
            if (enchantment != null && enchantment.isBound()) {
                ItemStack stack = this.menu.getSlot(0).getItem();
                Optional<Integer> color = Optional.ofNullable(stack.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY).findFirstCategory(enchantment)).flatMap(holder -> {
                    if (!holder.isBound())
                        return Optional.empty();
                    return Optional.of(holder.value().color().getValue());
                });
                return color.map(integer -> (E) Component.translatable("container.enchiridion.enchant.level_up", ((MutableComponent) Enchantment.getFullname(enchantment, menu.levelClue[i] - 1)).withColor(integer), ((MutableComponent) Enchantment.getFullname(enchantment, menu.levelClue[i])).withColor(integer))).orElseGet(() -> (E) Component.translatable("container.enchiridion.enchant.level_up", ((MutableComponent) Enchantment.getFullname(enchantment, menu.levelClue[i] - 1)), Enchantment.getFullname(enchantment, menu.levelClue[i])));
            }
        }
        return original;
    }

    @ModifyVariable(method = "render", at = @At(value = "LOAD", ordinal = 0), ordinal = 6)
    private int enchiridion$modifyLapisCountForLevelling(int original, @Local(ordinal = 3) int i) {
        if (((LevelUpEnchantmentMenuAccess)menu).enchiridion$getRequiredBookshelves(i) != -1) {
            Holder<Enchantment> enchantment = minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap().byId(menu.enchantClue[i]);
            if (enchantment != null && enchantment.isBound())
                return EnchantingTableUtil.getLapisCountForLevelling(menu.levelClue[i], enchantment.value().getMaxLevel());
        }
        return original;
    }

    @ModifyArg(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"), index = 2)
    private int enchiridion$slightlyPositionToTheLeft(int x) {
        if (((LevelUpEnchantmentMenuAccess)menu).enchiridion$getEnchantmentSize() > 0)
            return x - 4;
        return x;
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void enchiridion$renderLevelUpScrollBar(GuiGraphics graphics, float tickDelta, int mouseX, int mouseY, CallbackInfo ci) {
        if (((LevelUpEnchantmentMenuAccess)menu).enchiridion$getEnchantmentSize() > 0) {
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;
            graphics.blitSprite(EnchantingTableScreenUtil.SCROLLER_BACKGROUND_SPRITE, x + 164, y + 13, 0, 6, 59);
        }
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void enchiridion$renderLevelUpScrollBar(GuiGraphics graphics, int x, int y, float tickDelta, CallbackInfo ci) {
        if (((LevelUpEnchantmentMenuAccess)menu).enchiridion$getEnchantmentSize() > 0)
            EnchantingTableScreenUtil.renderScroller(graphics, (this.width - this.imageWidth) / 2, (this.height - this.imageHeight) / 2, ((LevelUpEnchantmentMenuAccess) menu).enchiridion$getEnchantmentSize(), ((LevelUpEnchantmentMenuAccess) menu).enchiridion$getScrollOffset());
    }

    @Inject(method = "mouseClicked", at = @At(value = "HEAD", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;mouseClicked(DDI)Z"), cancellable = true)
    private void enchiridion$handleDragging(double x, double y, int i, CallbackInfoReturnable<Boolean> cir) {
        EnchantingTableScreenUtil.setDragging(false);
        int originX = (this.width - this.imageWidth) / 2;
        int originY = (this.height - this.imageHeight) / 2;
        if (((LevelUpEnchantmentMenuAccess)menu).enchiridion$getEnchantmentSize() > 3
                && x > (double)(originX + 165)
                && x < (double)(originX + 165 + 4)
                && y > (double)(originY + 14)
                && y <= (double)(originY + 14 + 56)) {
            EnchantingTableScreenUtil.setDragging(true);
            cir.setReturnValue(true);
        }
    }

    @WrapWithCondition(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 3))
    private boolean enchiridion$dontHighlightWhenScrolling(GuiGraphics instance, ResourceLocation resourceLocation, int i, int j, int k, int l, @Local(argsOnly = true, ordinal = 0) int x, @Local(argsOnly = true, ordinal = 1) int y) {
        int originX = (this.width - this.imageWidth) / 2;
        int originY = (this.height - this.imageHeight) / 2;
        boolean bl = ((LevelUpEnchantmentMenuAccess)menu).enchiridion$getEnchantmentSize() > 3
                && x > (double)(originX + 165)
                && x < (double)(originX + 165 + 4)
                && y > (double)(originY + 14)
                && y <= (double)(originY + 14 + 56) || EnchantingTableScreenUtil.isDragging();
        if (bl) {
            instance.blitSprite(ENCHANTMENT_SLOT_SPRITE, i, j, k, l);
        }
        return !bl;
    }

    @ModifyVariable(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 5), ordinal = 10)
    private int enchiridion$dontHighlightTextWhenScrolling(int original, @Local(argsOnly = true, ordinal = 0) int x, @Local(argsOnly = true, ordinal = 1) int y) {
        int originX = (this.width - this.imageWidth) / 2;
        int originY = (this.height - this.imageHeight) / 2;
        if (original == 16777088 && (((LevelUpEnchantmentMenuAccess)menu).enchiridion$getEnchantmentSize() > 3
                && x > (double)(originX + 165)
                && x < (double)(originX + 165 + 4)
                && y > (double)(originY + 14)
                && y <= (double)(originY + 14 + 56) || EnchantingTableScreenUtil.isDragging())) {
            return 6839882;
        }
        return original;
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EnchantmentScreen;isHovering(IIIIDD)Z"))
    private boolean enchiridion$dontRenderEnchantmentTooltipWhenScrolling(boolean original, @Local(argsOnly = true, ordinal = 0) int x, @Local(argsOnly = true, ordinal = 1) int y) {
        int originX = (this.width - this.imageWidth) / 2;
        int originY = (this.height - this.imageHeight) / 2;
        return original && (((LevelUpEnchantmentMenuAccess)menu).enchiridion$getEnchantmentSize() < 1 ||
                !(x > (double)(originX + 165)
                && x < (double)(originX + 165 + 4)
                && y > (double)(originY + 14)
                && y <= (double)(originY + 14 + 56)) && !EnchantingTableScreenUtil.isDragging());
    }
}
