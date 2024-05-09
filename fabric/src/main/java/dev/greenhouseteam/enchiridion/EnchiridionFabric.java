package dev.greenhouseteam.enchiridion;

import com.mojang.datafixers.util.Pair;
import dev.greenhouseteam.enchiridion.enchantment.category.EnchantmentCategory;
import dev.greenhouseteam.enchiridion.enchantment.category.ItemEnchantmentCategories;
import dev.greenhouseteam.enchiridion.mixin.CreativeModeTabsAccessor;
import dev.greenhouseteam.enchiridion.mixin.fabric.CreativeModeTabsMixin;
import dev.greenhouseteam.enchiridion.platform.EnchiridionPlatformHelperFabric;
import dev.greenhouseteam.enchiridion.registry.EnchiridionDataComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentCategories;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionRegistries;
import dev.greenhouseteam.enchiridion.util.ClientRegistryAccessReference;
import dev.greenhouseteam.enchiridion.util.EnchiridionUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

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

            if (category != null && category.isBound() && !EnchiridionUtil.categoryAcceptsNewEnchantmentsWithValue(category, categories, enchantment))
                return TriState.FALSE;

            return TriState.DEFAULT;
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            Set<TagKey<Item>> itemTags = Set.of(Enchiridion.ItemTags.ASHES_ENCHANTABLE, Enchiridion.ItemTags.AXE_ENCHANTABLE);
            CreativeModeTabsAccessor.enchiridion$invokeGenerateEnchantmentBookTypesOnlyMaxLevel(entries, entries.getContext().holders().lookupOrThrow(Registries.ENCHANTMENT), itemTags, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            CreativeModeTabsAccessor.enchiridion$invokeGenerateEnchantmentBookTypesAllLevels(entries, entries.getContext().holders().lookupOrThrow(Registries.ENCHANTMENT), itemTags, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
        });

        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, entries) -> {
            sortEnchantmentsBasedOnCategory(entries.getDisplayStacks(), entries.getContext().holders());
            sortEnchantmentsBasedOnCategory(entries.getSearchTabStacks(), entries.getContext().holders());
        });

        FabricLoader.getInstance().getModContainer(Enchiridion.MOD_ID).ifPresent(modContainer -> ResourceManagerHelper.registerBuiltinResourcePack(Enchiridion.asResource("default_enchanted_books"), modContainer, Component.translatable("resourcePack.enchiridion.default_enchanted_books.name"), ResourcePackActivationType.NORMAL));
    }

    private static void sortEnchantmentsBasedOnCategory(List<ItemStack> stacks, HolderLookup.Provider provider) {
        if (stacks.stream().anyMatch(stack -> {
            ItemEnchantments enchantments = stack.getEnchantments();
            if (enchantments.isEmpty())
                enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            return !enchantments.isEmpty();
        })) {
            List<Pair<Integer, ItemStack>> indexList = new ArrayList<>(IntStream.range(0, stacks.size()).filter(i -> {
                ItemStack stack = stacks.get(i);
                ItemEnchantments enchantments = stack.getEnchantments();
                if (enchantments.isEmpty())
                    enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
                return !enchantments.isEmpty();
            }).mapToObj(i -> Pair.of(i, stacks.get(i))).toList());

            for (Pair<Integer, ItemStack> stack : indexList)
                stacks.set(stack.getFirst(), ItemStack.EMPTY);


            List<Integer> intList = indexList.stream().map(Pair::getFirst).toList();
            List<ItemStack> unmodifiableStackList = indexList.stream().map(Pair::getSecond).toList();
            List<ItemStack> stackList = new ArrayList<>(indexList.stream().map(Pair::getSecond).toList());

            stackList.sort((o1, o2) -> {
                if (o1.getItem() != o2.getItem())
                    return Integer.compare(unmodifiableStackList.indexOf(o1), unmodifiableStackList.indexOf(o2));

                ItemEnchantments enchantments = o1.getEnchantments();
                if (enchantments.isEmpty())
                    enchantments = o1.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
                ItemEnchantments enchantments2 = o2.getEnchantments();
                if (enchantments2.isEmpty())
                    enchantments2 = o2.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

                Pair<Holder<Enchantment>, Integer> enchantment = EnchiridionUtil.getFirstEnchantmentAndLevel(provider, enchantments);
                Pair<Holder<Enchantment>, Integer> enchantment2 = EnchiridionUtil.getFirstEnchantmentAndLevel(provider, enchantments2);


                int o1CategoryPriority = Optional.ofNullable(EnchiridionUtil.getFirstEnchantmentCategory(provider, enchantments, o1.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY))).map(category -> {
                    if (!category.isBound())
                        return Integer.MIN_VALUE;
                    return category.value().priority();
                }).orElse(0);
                int o2CategoryPriority = Optional.ofNullable(EnchiridionUtil.getFirstEnchantmentCategory(provider, enchantments2, o2.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY))).map(category -> {
                    if (!category.isBound())
                        return Integer.MIN_VALUE;
                    return category.value().priority();
                }).orElse(0);

                if (enchantment != null && enchantment2 != null && enchantment.getFirst().equals(enchantment2.getFirst()))
                    return Integer.compare(enchantment.getSecond(), enchantment2.getSecond());

                if (o1CategoryPriority == o2CategoryPriority && enchantment != null && enchantment2 != null)
                    return compareEnchantments(enchantment.getFirst(), enchantment2.getFirst());

                // We flip the typical comparison, so we can get higher priority to come first.
                return Integer.compare(o2CategoryPriority, o1CategoryPriority);
            });

            for (int i = 0; i < intList.size(); ++i)
                stacks.set(intList.get(i), stackList.get(i));
        }
    }

    private static int compareEnchantments(Holder<Enchantment> enchantment, Holder<Enchantment> enchantment2) {
        if (
                // Prioritise the Minecraft namespace.
                enchantment.unwrapKey().isPresent() && enchantment.unwrapKey().get().location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) &&
                        enchantment2.unwrapKey().isPresent() && !enchantment2.unwrapKey().get().location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)
        ) {
            return -1;
        } else if (
                // Prioritise the Minecraft namespace.
                enchantment.unwrapKey().isPresent() && !enchantment.unwrapKey().get().location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) &&
                        enchantment2.unwrapKey().isPresent() && enchantment2.unwrapKey().get().location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)
        ) {
            return 1;
        }
        if (enchantment.unwrapKey().isPresent() && enchantment2.unwrapKey().isPresent())
            return enchantment.unwrapKey().get().location().compareTo(enchantment2.unwrapKey().get().location());
        return Integer.compare(enchantment.hashCode(), enchantment2.hashCode());
    }

    public static RegistryAccess getRegistryAccess() {
        if (server == null)
            return ClientRegistryAccessReference.get();
        return server.registryAccess();
    }
}
