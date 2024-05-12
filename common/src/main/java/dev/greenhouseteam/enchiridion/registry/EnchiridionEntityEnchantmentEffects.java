package dev.greenhouseteam.enchiridion.registry;

import com.mojang.serialization.MapCodec;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.enchantment.effects.ExtinguishEffect;
import dev.greenhouseteam.enchiridion.enchantment.effects.FreezeEffect;
import dev.greenhouseteam.enchiridion.enchantment.effects.RidingEntityEffect;
import dev.greenhouseteam.enchiridion.registry.internal.RegistrationCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;

public class EnchiridionEntityEnchantmentEffects {
    public static void registerAll(RegistrationCallback<MapCodec<? extends EnchantmentEntityEffect>> callback) {
        callback.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Enchiridion.asResource("extinguish"), ExtinguishEffect.CODEC);
        callback.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Enchiridion.asResource("freeze"), FreezeEffect.CODEC);
        callback.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Enchiridion.asResource("riding_entity"), RidingEntityEffect.CODEC);
    }
}
