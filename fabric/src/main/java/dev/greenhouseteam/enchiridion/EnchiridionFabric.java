package dev.greenhouseteam.enchiridion;

import com.google.common.collect.ImmutableList;
import dev.greenhouseteam.enchiridion.enchantment.category.EnchantmentCategory;
import dev.greenhouseteam.enchiridion.enchantment.category.ItemEnchantmentCategories;
import dev.greenhouseteam.enchiridion.mixin.CreativeModeTabsAccessor;
import dev.greenhouseteam.enchiridion.mixin.fabric.LootPoolAccessor;
import dev.greenhouseteam.enchiridion.mixin.fabric.LootTableBuilderAccessor;
import dev.greenhouseteam.enchiridion.network.clientbound.SyncEnchantScrollIndexClientboundPacket;
import dev.greenhouseteam.enchiridion.network.clientbound.SyncEnchantedFrozenStateClientboundPacket;
import dev.greenhouseteam.enchiridion.network.clientbound.SyncEnchantmentLevelUpSeedsPacket;
import dev.greenhouseteam.enchiridion.network.serverbound.SyncEnchantScrollIndexServerboundPacket;
import dev.greenhouseteam.enchiridion.platform.EnchiridionPlatformHelperFabric;
import dev.greenhouseteam.enchiridion.registry.EnchiridionDataComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantments;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEntityEnchantmentEffects;
import dev.greenhouseteam.enchiridion.registry.EnchiridionRegistries;
import dev.greenhouseteam.enchiridion.util.AnvilUtil;
import dev.greenhouseteam.enchiridion.util.ClientRegistryAccessReference;
import dev.greenhouseteam.enchiridion.util.CreativeTabUtil;
import dev.greenhouseteam.enchiridion.util.EnchiridionUtil;
import dev.greenhouseteam.enchiridion.util.TagUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EnchiridionFabric implements ModInitializer {
    public static MinecraftServer server;
    private static ResourceManager lootTableResourceManager;
    private static HolderLookup.Provider lootTableAccess;

    public static void setLootTableResourceManager(ResourceManager manager) {
        lootTableResourceManager = manager;
    }

    public static void setLootTableAccess(RegistryAccess access) {
        lootTableAccess = access;
    }

    @Override
    public void onInitialize() {
        Enchiridion.init(new EnchiridionPlatformHelperFabric());

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            Set<TagKey<Item>> itemTags = Set.of(
                    Enchiridion.ItemTags.ASHES_ENCHANTABLE,
                    Enchiridion.ItemTags.AXE_ENCHANTABLE,
                    Enchiridion.ItemTags.ICE_STRIKE_ENCHANTABLE,
                    Enchiridion.ItemTags.ICE_STRIKE_PRIMARY_ENCHANTABLE,
                    Enchiridion.ItemTags.PICKAXE_ENCHANTABLE);
            CreativeModeTabsAccessor.enchiridion$invokeGenerateEnchantmentBookTypesOnlyMaxLevel(entries, entries.getContext().holders().lookupOrThrow(Registries.ENCHANTMENT), itemTags, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            CreativeModeTabsAccessor.enchiridion$invokeGenerateEnchantmentBookTypesAllLevels(entries, entries.getContext().holders().lookupOrThrow(Registries.ENCHANTMENT), itemTags, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
        });

        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, entries) -> {
            CreativeTabUtil.sortEnchantmentsBasedOnCategory(entries.getDisplayStacks(), entries.getContext().holders());
            CreativeTabUtil.sortEnchantmentsBasedOnCategory(entries.getSearchTabStacks(), entries.getContext().holders());
        });

        LootTableEvents.MODIFY.register((key, table, source) -> {
            ResourceLocation location = key.location();
            if (location.getPath().startsWith("blocks/")) {
                Optional<Holder.Reference<Block>> blockHolder = lootTableAccess.lookupOrThrow(Registries.BLOCK).get(ResourceKey.create(Registries.BLOCK, location.withPath(string -> string.split("/", 2)[1])));
                Optional<Holder.Reference<Enchantment>> enchantmentHolder = lootTableAccess.lookupOrThrow(Registries.ENCHANTMENT).get(EnchiridionEnchantments.CRUMBLE);
                if (enchantmentHolder.isPresent() && blockHolder.isPresent() && TagUtil.isInBaseStoneTag(blockHolder.get(), lootTableResourceManager, lootTableAccess.lookupOrThrow(Registries.BLOCK))) {
                    List<LootPool> pools = ((LootTableBuilderAccessor)table).enchiridion$getPools().build()
                            .stream().peek(pool -> {
                                List<LootItemCondition> conditions = new ArrayList<>(pool.conditions);
                                EnchantmentPredicate predicate = new EnchantmentPredicate(enchantmentHolder.get(), MinMaxBounds.Ints.atLeast(1));
                                conditions.add(InvertedLootItemCondition.invert(MatchTool.toolMatches(
                                        ItemPredicate.Builder.item()
                                                .withSubPredicate(
                                                        ItemSubPredicates.ENCHANTMENTS,
                                                        ItemEnchantmentsPredicate.enchantments(
                                                                List.of(predicate)
                                                        )
                                                ))
                                ).build());
                                ((LootPoolAccessor)pool).enchiridion$setConditions(ImmutableList.copyOf(conditions));
                                ((LootPoolAccessor)pool).enchiridion$setCompositeCondition(Util.allOf(conditions));
                            }).toList();
                    ImmutableList.Builder<LootPool> poolBuilder = ImmutableList.builder();
                    poolBuilder.addAll(pools);
                    ((LootTableBuilderAccessor)table).enchiridion$setPools(poolBuilder);
                }
            }
        });

        registerContents();
        registerPackets();

        ServerLifecycleEvents.SERVER_STARTING.register(server1 -> server = server1);
        ServerLifecycleEvents.SERVER_STOPPED.register(server1 -> server = null);

        EnchantmentEvents.ALLOW_ENCHANTING.register((enchantment, target, enchantingContext) -> {
            ItemEnchantmentCategories categories = target.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY);
            Holder<EnchantmentCategory> category = EnchiridionUtil.getFirstEnchantmentCategory(getRegistryAccess(), enchantment);

            if (category != null && !AnvilUtil.getAnvilContext() && !EnchiridionUtil.categoryAcceptsNewEnchantmentsWithValue(category, categories, enchantment))
                return TriState.FALSE;

            return TriState.DEFAULT;
        });

        FabricLoader.getInstance().getModContainer(Enchiridion.MOD_ID).ifPresent(modContainer -> {
            ResourceManagerHelper.registerBuiltinResourcePack(Enchiridion.asResource("default_enchanted_books"), modContainer, Component.translatable("resourcePack.enchiridion.default_enchanted_books.name"), ResourcePackActivationType.NORMAL);
            ResourceManagerHelper.registerBuiltinResourcePack(Enchiridion.asResource("vanilla_enchantment_modifications"), modContainer, Component.translatable("dataPack.enchiridion.vanilla_enchantment_modifications.name"), ResourcePackActivationType.DEFAULT_ENABLED);
        });
    }

    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(SyncEnchantScrollIndexClientboundPacket.TYPE, SyncEnchantScrollIndexClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncEnchantedFrozenStateClientboundPacket.TYPE, SyncEnchantedFrozenStateClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncEnchantmentLevelUpSeedsPacket.TYPE, SyncEnchantmentLevelUpSeedsPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SyncEnchantScrollIndexServerboundPacket.TYPE, SyncEnchantScrollIndexServerboundPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SyncEnchantScrollIndexServerboundPacket.TYPE, (payload, context) -> payload.handle(context.player()));
    }

    public static void registerContents() {
        EnchiridionDataComponents.registerAll(Registry::register);
        EnchiridionEnchantmentEffectComponents.registerAll(Registry::register);
        EnchiridionEntityEnchantmentEffects.registerAll(Registry::register);

        DynamicRegistries.registerSynced(EnchiridionRegistries.ENCHANTMENT_CATEGORY, EnchantmentCategory.DIRECT_CODEC);
    }

    public static RegistryAccess getRegistryAccess() {
        if (server == null || !server.isDedicatedServer())
            return ClientRegistryAccessReference.get(server);
        return server.registryAccess();
    }
}
