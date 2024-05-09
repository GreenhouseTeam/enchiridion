package dev.greenhouseteam.enchiridion.registry;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.enchantment.category.EnchantmentCategory;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class EnchiridionRegistries {
    public static final ResourceKey<Registry<EnchantmentCategory>> ENCHANTMENT_CATEGORY = ResourceKey.createRegistryKey(Enchiridion.asResource("enchantment_category"));
}
