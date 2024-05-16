package dev.greenhouseteam.enchiridion.client.util;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.access.LevelUpEnchantmentMenuAccess;
import dev.greenhouseteam.enchiridion.mixin.client.AbstractContainerScreenAccessor;
import dev.greenhouseteam.enchiridion.network.serverbound.SyncEnchantScrollIndexServerboundPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class EnchantingTableScreenUtil {
    private static boolean dragging = false;

    public static boolean isDragging() {
        return dragging;
    }

    public static void setDragging(boolean value) {
        dragging = value;
    }

    public static final ResourceLocation SCROLLER_SPRITE = Enchiridion.asResource("container/enchanting_table/scroller");
    public static final ResourceLocation SCROLLER_DISABLED_SPRITE = Enchiridion.asResource("container/enchanting_table/scroller_disabled");
    public static final ResourceLocation SCROLLER_BACKGROUND_SPRITE = Enchiridion.asResource("container/enchanting_table/scroller_background");

    public static void renderScroller(GuiGraphics graphics, int x, int y, int enchantmentSize, double scrollOffset) {
        int size = enchantmentSize + 1 - 3;
        if (size > 1) {
            int maximum = 56 - (12 + (size - 1) * 56 / size);
            int multiplier = 1 + maximum / size + 56 / size;
            int clamped = Math.min(43, Mth.floor(scrollOffset)) * multiplier;
            if ((int)scrollOffset == size - 1)
                clamped = 42;

            graphics.blitSprite(SCROLLER_SPRITE, x + 165, y + 14 + clamped, 0, 4, 14);
        } else
            graphics.blitSprite(SCROLLER_DISABLED_SPRITE, x + 165, y + 14, 0, 4, 14);
    }

    public static boolean scrollScroller(EnchantmentScreen enchantmentScreen, double scroll) {
        int size = ((LevelUpEnchantmentMenuAccess)enchantmentScreen.getMenu()).enchiridion$getEnchantmentSize();
        if (size > 3) {
            int max = size - 3;
            int originalScrollIndex = (int) ((LevelUpEnchantmentMenuAccess) enchantmentScreen.getMenu()).enchiridion$getScrollOffset();
            int finalScrollVal = Mth.clamp((int)(((LevelUpEnchantmentMenuAccess) enchantmentScreen.getMenu()).enchiridion$getScrollOffset() - scroll), 0, max);
            ((LevelUpEnchantmentMenuAccess) enchantmentScreen.getMenu()).enchiridion$setScrollOffset(finalScrollVal);
            if (finalScrollVal != originalScrollIndex)
                Enchiridion.getHelper().sendServerbound(new SyncEnchantScrollIndexServerboundPacket(finalScrollVal));
            return true;
        }
        return false;
    }

    public static void dragScroller(EnchantmentScreen enchantmentScreen, double y) {
        int size = ((LevelUpEnchantmentMenuAccess) enchantmentScreen.getMenu()).enchiridion$getEnchantmentSize();
        if (EnchantingTableScreenUtil.isDragging() && size > 3) {
            int min = ((AbstractContainerScreenAccessor)enchantmentScreen).enchiridion$getTopPos() + 18;
            int max = min + 56;
            int scrollable = size - 3;
            float scrollVal = ((float)y - (float) min - 3.0F) / ((float) (max - min) - 14.0F);
            scrollVal = scrollVal * (float) scrollable + 0.5F;
            int originalScrollIndex = (int) ((LevelUpEnchantmentMenuAccess) enchantmentScreen.getMenu()).enchiridion$getScrollOffset();
            int finalScrollVal = Mth.clamp((int) scrollVal, 0, scrollable);
            ((LevelUpEnchantmentMenuAccess) enchantmentScreen.getMenu()).enchiridion$setScrollOffset(finalScrollVal);
            if (finalScrollVal != originalScrollIndex)
                Enchiridion.getHelper().sendServerbound(new SyncEnchantScrollIndexServerboundPacket(finalScrollVal));
        }
    }
}
