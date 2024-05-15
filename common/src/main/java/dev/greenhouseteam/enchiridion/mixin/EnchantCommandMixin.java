package dev.greenhouseteam.enchiridion.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.greenhouseteam.enchiridion.enchantment.category.EnchantmentCategory;
import dev.greenhouseteam.enchiridion.enchantment.category.ItemEnchantmentCategories;
import dev.greenhouseteam.enchiridion.registry.EnchiridionDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Collection;
import java.util.List;

@Mixin(EnchantCommand.class)
public class EnchantCommandMixin {
    @ModifyArg(method = { "method_51964", "lambda$enchant$7" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"), index = 1)
    private static Object[] enchiridion$colorEnchantCommandName(Object[] original, @Local(argsOnly = true) Holder<Enchantment> enchantment, @Local(argsOnly = true) Collection<? extends Entity> entities) {
        List<? extends Entity> entityList = List.copyOf(entities);
        if (entityList.getFirst() instanceof LivingEntity living) {
            Holder<EnchantmentCategory> category = living.getMainHandItem().getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY).findFirstCategory(enchantment);
            if (category != null && category.isBound())
                original[0] = ((MutableComponent) original[0]).withColor(category.value().color().getValue());
        }
        return original;
    }

    @ModifyArg(method = { "method_51963", "lambda$enchant$8" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"), index = 1)
    private static Object[] enchiridion$colorEnchantCommandNameMultiple(Object[] original, @Local(argsOnly = true) Holder<Enchantment> enchantment, @Local(argsOnly = true) Collection<? extends Entity> entities) {
        List<? extends Entity> entityList = List.copyOf(entities);
        if (entityList.getFirst() instanceof LivingEntity living) {
            Holder<EnchantmentCategory> category = living.getMainHandItem().getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY).findFirstCategory(enchantment);
            if (category != null && category.isBound())
                original[0] = ((MutableComponent) original[0]).withColor(category.value().color().getValue());
        }
        return original;
    }
}
