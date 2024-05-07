package dev.greenhouseteam.enchiridion.registry;

import dev.greenhouseteam.enchiridion.Enchiridion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchiridionEnchantments {
    public static final ResourceKey<Enchantment> REACH = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("reach"));

    public static final ResourceKey<Enchantment> ASHES_CURSE = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("ashes_curse"));
}
