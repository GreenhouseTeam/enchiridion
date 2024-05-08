package dev.greenhouseteam.enchiridion.registry;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.registry.internal.RegistrationCallback;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;

import java.util.List;

public class EnchiridionEnchantmentEffectComponents {
    public static final DataComponentType<List<ConditionalEffect<EnchantmentEntityEffect>>> POST_BLOCK_DROP = DataComponentType.<List<ConditionalEffect<EnchantmentEntityEffect>>>builder()
            .persistent(ConditionalEffect.codec(EnchantmentEntityEffect.CODEC, EnchiridionLootContextParamSets.BLOCK_DROP).listOf())
            .build();

    public static void registerAll(RegistrationCallback<DataComponentType<?>> callback) {
        callback.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Enchiridion.asResource("post_block_drop"), POST_BLOCK_DROP);
    }
}
