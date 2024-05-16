package dev.greenhouseteam.enchiridion.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.greenhouseteam.enchiridion.client.util.EnchantingTableScreenUtil;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMixin {
    @ModifyReturnValue(method = "mouseScrolled", at = @At("RETURN"))
    private boolean enchiridion$scrollEnchantingLevelUpMenuMenu(boolean original, double d, double e, double f, double scroll) {
        if ((ContainerEventHandler)(Object)this instanceof EnchantmentScreen enchantmentScreen)
            if (EnchantingTableScreenUtil.scrollScroller(enchantmentScreen, scroll))
                return true;
        return original;
    }
}
