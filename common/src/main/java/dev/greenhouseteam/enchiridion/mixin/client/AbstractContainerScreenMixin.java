package dev.greenhouseteam.enchiridion.mixin.client;

import dev.greenhouseteam.enchiridion.client.util.EnchantingTableScreenUtil;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Inject(method = "mouseDragged", at = @At("RETURN"))
    private void enchiridion$dragEnchantingScrollBar(double x, double y, int key, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
        if ((ContainerEventHandler)(Object)this instanceof EnchantmentScreen enchantmentScreen)
            EnchantingTableScreenUtil.dragScroller(enchantmentScreen, y);
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void enchiridion$resetDragging(double x, double y, int i, CallbackInfoReturnable<Boolean> cir) {
        if ((ContainerEventHandler)(Object)this instanceof EnchantmentScreen enchantmentScreen)
            EnchantingTableScreenUtil.setDragging(false);
    }
}
