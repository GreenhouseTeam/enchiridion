package dev.greenhouseteam.enchiridion.registry;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.registry.internal.RegistrationCallback;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Unit;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;

public class EnchiridionEnchantmentEffectComponents {
    public static final DataComponentType<List<ConditionalEffect<EnchantmentEntityEffect>>> POST_BLOCK_DROP = DataComponentType.<List<ConditionalEffect<EnchantmentEntityEffect>>>builder()
            .persistent(ConditionalEffect.codec(EnchantmentEntityEffect.CODEC, EnchiridionLootContextParamSets.ENCHANTED_BLOCK_DROP).listOf())
            .build();
    public static final DataComponentType<List<ConditionalEffect<EnchantmentEntityEffect>>> POST_ENTITY_DROP = DataComponentType.<List<ConditionalEffect<EnchantmentEntityEffect>>>builder()
            .persistent(ConditionalEffect.codec(EnchantmentEntityEffect.CODEC, EnchiridionLootContextParamSets.ENCHANTED_ENTITY_DROP).listOf())
            .build();
    public static final DataComponentType<List<ConditionalEffect<Unit>>> ALLOW_FIRING_WITHOUT_PROJECTILE = DataComponentType.<List<ConditionalEffect<Unit>>>builder()
            .persistent(ConditionalEffect.codec(Unit.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf())
            .build();

    public static void registerAll(RegistrationCallback<DataComponentType<?>> callback) {
        callback.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Enchiridion.asResource("post_block_drop"), POST_BLOCK_DROP);
        callback.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Enchiridion.asResource("post_entity_drop"), POST_ENTITY_DROP);
        callback.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Enchiridion.asResource("allow_firing_without_projectile"), ALLOW_FIRING_WITHOUT_PROJECTILE);
    }
}
