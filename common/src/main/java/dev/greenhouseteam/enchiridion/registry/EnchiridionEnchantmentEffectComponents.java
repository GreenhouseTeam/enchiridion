package dev.greenhouseteam.enchiridion.registry;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.enchantment.effects.PreventHungerConsumptionEffect;
import dev.greenhouseteam.enchiridion.registry.internal.RegistrationCallback;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Unit;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.TargetedConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;

public class EnchiridionEnchantmentEffectComponents {
    public static final DataComponentType<List<ConditionalEffect<Unit>>> ALLOW_FIRING_WITHOUT_PROJECTILE = DataComponentType.<List<ConditionalEffect<Unit>>>builder()
            .persistent(ConditionalEffect.codec(Unit.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf())
            .build();
    public static final DataComponentType<List<ConditionalEffect<EnchantmentEntityEffect>>> POST_BLOCK_DROP = DataComponentType.<List<ConditionalEffect<EnchantmentEntityEffect>>>builder()
            .persistent(ConditionalEffect.codec(EnchantmentEntityEffect.CODEC, EnchiridionLootContextParamSets.ENCHANTED_BLOCK_DROP).listOf())
            .build();
    public static final DataComponentType<List<ConditionalEffect<EnchantmentEntityEffect>>> POST_ENTITY_DROP = DataComponentType.<List<ConditionalEffect<EnchantmentEntityEffect>>>builder()
            .persistent(ConditionalEffect.codec(EnchantmentEntityEffect.CODEC, EnchiridionLootContextParamSets.ENCHANTED_ENTITY_DROP).listOf())
            .build();
    public static final DataComponentType<List<TargetedConditionalEffect<EnchantmentEntityEffect>>> POST_SHIELD_DISABLE = DataComponentType.<List<TargetedConditionalEffect<EnchantmentEntityEffect>>>builder()
            .persistent(TargetedConditionalEffect.codec(EnchantmentEntityEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
            .build();
    public static final DataComponentType<List<ConditionalEffect<PreventHungerConsumptionEffect>>> PREVENT_HUNGER_CONSUMPTION = DataComponentType.<List<ConditionalEffect<PreventHungerConsumptionEffect>>>builder()
            .persistent(ConditionalEffect.codec(PreventHungerConsumptionEffect.CODEC, LootContextParamSets.ENCHANTED_ENTITY).listOf())
            .build();
    public static final DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> VEHICLE_DAMAGE_PROTECTION = DataComponentType.<List<ConditionalEffect<EnchantmentValueEffect>>>builder()
            .persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, EnchiridionLootContextParamSets.VEHICLE_ENCHANTED_DAMAGE).listOf())
            .build();

    public static void registerAll(RegistrationCallback<DataComponentType<?>> callback) {
        callback.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Enchiridion.asResource("allow_firing_without_projectile"), ALLOW_FIRING_WITHOUT_PROJECTILE);
        callback.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Enchiridion.asResource("post_block_drop"), POST_BLOCK_DROP);
        callback.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Enchiridion.asResource("post_entity_drop"), POST_ENTITY_DROP);
        callback.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Enchiridion.asResource("post_shield_disable"), POST_SHIELD_DISABLE);
        callback.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Enchiridion.asResource("prevent_hunger_consumption"), PREVENT_HUNGER_CONSUMPTION);
        callback.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Enchiridion.asResource("vehicle_damage_protection"), VEHICLE_DAMAGE_PROTECTION);
    }
}
