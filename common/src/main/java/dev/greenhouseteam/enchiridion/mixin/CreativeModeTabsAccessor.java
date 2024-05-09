package dev.greenhouseteam.enchiridion.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(CreativeModeTabs.class)
public interface CreativeModeTabsAccessor {
    @Invoker("generateEnchantmentBookTypesOnlyMaxLevel")
    static void enchiridion$invokeGenerateEnchantmentBookTypesOnlyMaxLevel(CreativeModeTab.Output output, HolderLookup<Enchantment> holderLookup, Set<TagKey<Item>> set, CreativeModeTab.TabVisibility tabVisibility) {

    }

    @Invoker("generateEnchantmentBookTypesAllLevels")
    static void enchiridion$invokeGenerateEnchantmentBookTypesAllLevels(CreativeModeTab.Output output, HolderLookup<Enchantment> holderLookup, Set<TagKey<Item>> set, CreativeModeTab.TabVisibility tabVisibility) {

    }
}
