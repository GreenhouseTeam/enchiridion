package dev.greenhouseteam.enchiridion.registry;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.registry.internal.RegistrationCallback;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;

import java.util.List;

public class EnchiridionEnchantmentEffectComponents {
    public static final DataComponentType<List<ConditionalEffect<EnchantmentEntityEffect>>> POST_EQUIPMENT_DROPS = DataComponentType.<List<ConditionalEffect<EnchantmentEntityEffect>>>builder()
            .persistent(ConditionalEffect.codec(EnchantmentEntityEffect.CODEC, EnchiridionLootContextParamSets.ENCHANTED_DROPS).listOf())
            .build();

    public static void registerAll(RegistrationCallback<DataComponentType<?>> callback) {
        callback.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Enchiridion.asResource("post_equipment_drops"), POST_EQUIPMENT_DROPS);
    }
}
