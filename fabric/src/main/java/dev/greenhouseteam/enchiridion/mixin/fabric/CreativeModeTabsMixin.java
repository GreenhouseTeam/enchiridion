package dev.greenhouseteam.enchiridion.mixin.fabric;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import dev.greenhouseteam.enchiridion.util.EnchiridionUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(CreativeModeTabs.class)
public abstract class CreativeModeTabsMixin {
    @Unique
    private static HolderLookup.Provider enchiridion$enchantmentLookupProvider;

    @Inject(method = "method_51321", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab$ItemDisplayParameters;holders()Lnet/minecraft/core/HolderLookup$Provider;"))
    private static void enchiridion$shareGenerateMaxLevelProvider(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output, CallbackInfo ci) {
        enchiridion$enchantmentLookupProvider = parameters.holders();
    }

    @ModifyReceiver(method = "generateEnchantmentBookTypesOnlyMaxLevel", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    private static Stream<Holder.Reference<Enchantment>> enchiridion$sortMaxEnchantedBooksByCategory(Stream<Holder.Reference<Enchantment>> original, Predicate<? super Holder.Reference<Enchantment>> predicate, CreativeModeTab.Output output, HolderLookup<Enchantment> holderLookup, Set<TagKey<Item>> set, CreativeModeTab.TabVisibility tabVisibility) {
        return original;
    }

    @ModifyReceiver(method = "generateEnchantmentBookTypesAllLevels", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    private static Stream<Holder.Reference<Enchantment>> enchiridion$sortEnchantedBooksByCategory(Stream<Holder.Reference<Enchantment>> original, Predicate<? super Holder.Reference<Enchantment>> predicate, CreativeModeTab.Output output, HolderLookup<Enchantment> holderLookup, Set<TagKey<Item>> set, CreativeModeTab.TabVisibility tabVisibility) {
        Stream<Holder.Reference<Enchantment>> stream = original.sorted((o1, o2) -> {
            if (!o1.isBound())
                return -1;
            if (!o2.isBound())
                return 1;

            int o1CategoryPriority = Optional.ofNullable(EnchiridionUtil.getFirstEnchantmentCategory(enchiridion$enchantmentLookupProvider, o1)).map(category -> {
                if (!category.isBound())
                    return 0;
                return category.value().priority();
            }).orElse(0);
            int o2CategoryPriority = Optional.ofNullable(EnchiridionUtil.getFirstEnchantmentCategory(enchiridion$enchantmentLookupProvider, o2)).map(category -> {
                if (!category.isBound())
                    return 0;
                return category.value().priority();
            }).orElse(0);

            return Integer.compare(o1CategoryPriority, o2CategoryPriority);
        });
        enchiridion$enchantmentLookupProvider = null;
        return stream;
    }
}
