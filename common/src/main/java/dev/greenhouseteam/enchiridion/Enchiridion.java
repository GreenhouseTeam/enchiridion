package dev.greenhouseteam.enchiridion;

import dev.greenhouseteam.enchiridion.platform.EnchiridionPlatformHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Enchiridion {
    public static final String MOD_ID = "enchiridion";
    public static final String MOD_NAME = "Enchiridion";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
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
    }
}