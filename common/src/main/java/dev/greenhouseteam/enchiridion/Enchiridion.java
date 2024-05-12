package dev.greenhouseteam.enchiridion;

import dev.greenhouseteam.enchiridion.platform.EnchiridionPlatformHelper;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Enchiridion {
    public static final String MOD_ID = "enchiridion";
    public static final String MOD_NAME = "Enchiridion";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    private static EnchiridionPlatformHelper helper;

    public static void init(EnchiridionPlatformHelper helper) {
        Enchiridion.helper = helper;
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static EnchiridionPlatformHelper getHelper() {
        return helper;
    }

    public static class BlockTags {
        public static final TagKey<Block> BASE_STONE = TagKey.create(Registries.BLOCK, asResource("base_stone"));
    }

    public static class EnchantmentTags {
        public static final TagKey<Enchantment> PRIMARY_CATEGORY = TagKey.create(Registries.ENCHANTMENT, asResource("category/primary"));
        public static final TagKey<Enchantment> SECONDARY_CATEGORY = TagKey.create(Registries.ENCHANTMENT, asResource("category/secondary"));
        public static final TagKey<Enchantment> TERTIARY_CATEGORY = TagKey.create(Registries.ENCHANTMENT, asResource("category/tertiary"));
        public static final TagKey<Enchantment> UNCATEGORISED_CATEGORY = TagKey.create(Registries.ENCHANTMENT, asResource("category/uncategorised"));

        public static final TagKey<Enchantment> ELEMENTAL_EXCLUSIVE = TagKey.create(Registries.ENCHANTMENT, asResource("exclusive_set/elemental"));
    }

    public static class ItemTags {
        public static final TagKey<Item> ASHES_ENCHANTABLE = TagKey.create(Registries.ITEM, asResource("enchantable/ashes"));
        public static final TagKey<Item> AXE_ENCHANTABLE = TagKey.create(Registries.ITEM, asResource("enchantable/axe"));
        public static final TagKey<Item> PICKAXE_ENCHANTABLE = TagKey.create(Registries.ITEM, asResource("enchantable/pickaxe"));
        public static final TagKey<Item> ICE_STRIKE_ENCHANTABLE = TagKey.create(Registries.ITEM, asResource("enchantable/ice_strike"));
        public static final TagKey<Item> ICE_STRIKE_PRIMARY_ENCHANTABLE = TagKey.create(Registries.ITEM, asResource("enchantable/ice_strike_primary"));
    }
}