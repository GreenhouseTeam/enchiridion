package dev.greenhouseteam.enchiridion;

import dev.greenhouseteam.enchiridion.enchantment.category.EnchantmentCategory;
import dev.greenhouseteam.enchiridion.enchantment.category.ItemEnchantmentCategories;
import dev.greenhouseteam.enchiridion.mixin.CreativeModeTabsAccessor;
import dev.greenhouseteam.enchiridion.platform.EnchiridionPlatformHelperFabric;
import dev.greenhouseteam.enchiridion.registry.EnchiridionDataComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionRegistries;
import dev.greenhouseteam.enchiridion.util.ClientRegistryAccessReference;
import dev.greenhouseteam.enchiridion.util.CreativeTabUtil;
import dev.greenhouseteam.enchiridion.util.EnchiridionUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.Set;

public class EnchiridionFabric implements ModInitializer {
    public static MinecraftServer server;

    @Override
    public void onInitialize() {
        Enchiridion.init(new EnchiridionPlatformHelperFabric());

        DynamicRegistries.registerSynced(EnchiridionRegistries.ENCHANTMENT_CATEGORY, EnchantmentCategory.DIRECT_CODEC);

        EnchiridionDataComponents.registerAll(Registry::register);
        EnchiridionEnchantmentEffectComponents.registerAll(Registry::register);

        ServerLifecycleEvents.SERVER_STARTING.register(server1 -> server = server1);

        EnchantmentEvents.ALLOW_ENCHANTING.register((enchantment, target, enchantingContext) -> {
            ItemEnchantmentCategories categories = target.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY);
            Holder<EnchantmentCategory> category = categories.findFirstCategory(enchantment);

            if (category != null && !EnchiridionUtil.categoryAcceptsNewEnchantmentsWithValue(category, categories, enchantment))
                return TriState.FALSE;

            return TriState.DEFAULT;
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            Set<TagKey<Item>> itemTags = Set.of(Enchiridion.ItemTags.ASHES_ENCHANTABLE, Enchiridion.ItemTags.AXE_ENCHANTABLE);
            CreativeModeTabsAccessor.enchiridion$invokeGenerateEnchantmentBookTypesOnlyMaxLevel(entries, entries.getContext().holders().lookupOrThrow(Registries.ENCHANTMENT), itemTags, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            CreativeModeTabsAccessor.enchiridion$invokeGenerateEnchantmentBookTypesAllLevels(entries, entries.getContext().holders().lookupOrThrow(Registries.ENCHANTMENT), itemTags, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
        });

        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, entries) -> {
            CreativeTabUtil.sortEnchantmentsBasedOnCategory(entries.getDisplayStacks(), entries.getContext().holders());
            CreativeTabUtil.sortEnchantmentsBasedOnCategory(entries.getSearchTabStacks(), entries.getContext().holders());
        });

        FabricLoader.getInstance().getModContainer(Enchiridion.MOD_ID).ifPresent(modContainer -> ResourceManagerHelper.registerBuiltinResourcePack(Enchiridion.asResource("default_enchanted_books"), modContainer, Component.translatable("resourcePack.enchiridion.default_enchanted_books.name"), ResourcePackActivationType.NORMAL));
    }

    public static RegistryAccess getRegistryAccess() {
        if (server == null)
            return ClientRegistryAccessReference.get();
        return server.registryAccess();
    }
}
