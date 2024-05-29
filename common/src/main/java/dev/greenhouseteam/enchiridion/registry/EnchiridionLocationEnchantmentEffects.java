package dev.greenhouseteam.enchiridion.registry;

import com.mojang.serialization.MapCodec;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.enchantment.effects.HighlightMinedBlocksEffect;
import dev.greenhouseteam.enchiridion.registry.internal.RegistrationCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;

public class EnchiridionLocationEnchantmentEffects {
    public static void registerAll(RegistrationCallback<MapCodec<? extends EnchantmentLocationBasedEffect>> callback) {
        callback.register(BuiltInRegistries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE, Enchiridion.asResource("highlight_blocks"), HighlightMinedBlocksEffect.CODEC);
    }
}
