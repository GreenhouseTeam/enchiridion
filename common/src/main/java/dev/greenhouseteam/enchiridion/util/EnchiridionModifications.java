package dev.greenhouseteam.enchiridion.util;

import com.google.gson.JsonElement;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;
import java.util.Optional;

public class EnchiridionModifications {
    // I wish data packs would be patchable.
    public static Enchantment modifyEnchantments(ResourceKey<Enchantment> enchantmentKey, Enchantment enchantment, RegistryOps<JsonElement> ops) {
        if (enchantmentKey == Enchantments.INFINITY)
            return modifyInfinity(enchantment);
        return enchantment;
    }

    public static Enchantment modifyInfinity(Enchantment enchantment) {
        DataComponentMap.Builder builder = createDataComponentMapFromEnchantment(enchantment);
        builder.set(EnchiridionEnchantmentEffectComponents.ALLOW_FIRING_WITHOUT_PROJECTILE, List.of(new ConditionalEffect<>(Unit.INSTANCE, Optional.empty())));
        return new Enchantment(enchantment.description(), enchantment.definition(), enchantment.exclusiveSet(), builder.build());
    }

    public static DataComponentMap.Builder createDataComponentMapFromEnchantment(Enchantment enchantment) {
        DataComponentMap.Builder builder = DataComponentMap.builder();
        enchantment.effects().forEach(typedDataComponent -> builder.set((DataComponentType<Object>) typedDataComponent.type(), typedDataComponent.value()));
        return builder;
    }
}
