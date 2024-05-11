package dev.greenhouseteam.enchiridion;

import dev.greenhouseteam.enchiridion.platform.EnchiridionPlatformHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static class ItemTags {
        public static final TagKey<Item> ASHES_ENCHANTABLE = TagKey.create(Registries.ITEM, asResource("enchantable/ashes"));
        public static final TagKey<Item> AXE_ENCHANTABLE = TagKey.create(Registries.ITEM, asResource("enchantable/axe"));
        public static final TagKey<Item> ICE_CRUSH_ENCHANTABLE = TagKey.create(Registries.ITEM, asResource("enchantable/ice_crush"));
        public static final TagKey<Item> ICE_CRUSH_PRIMARY_ENCHANTABLE = TagKey.create(Registries.ITEM, asResource("enchantable/ice_crush_primary"));
    }

    public static class EnchantmentTags {
        public static final TagKey<Enchantment> PRIMARY_CATEGORY = TagKey.create(Registries.ENCHANTMENT, asResource("category/primary"));
        public static final TagKey<Enchantment> SECONDARY_CATEGORY = TagKey.create(Registries.ENCHANTMENT, asResource("category/secondary"));
        public static final TagKey<Enchantment> TERTIARY_CATEGORY = TagKey.create(Registries.ENCHANTMENT, asResource("category/tertiary"));
        public static final TagKey<Enchantment> UNCATEGORISED_CATEGORY = TagKey.create(Registries.ENCHANTMENT, asResource("category/uncategorised"));

        public static final TagKey<Enchantment> ELEMENTAL_EXCLUSIVE = TagKey.create(Registries.ENCHANTMENT, asResource("exclusive_set/elemental"));
    }
}