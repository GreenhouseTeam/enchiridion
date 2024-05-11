package dev.greenhouseteam.enchiridion.util;

import com.google.gson.JsonElement;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.TargetedConditionalEffect;
import net.minecraft.world.item.enchantment.effects.AllOf;
import net.minecraft.world.item.enchantment.effects.DamageItem;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnchiridionModifications {
    // I wish data packs would be patchable.
    public static Enchantment modifyEnchantments(ResourceKey<Enchantment> enchantmentKey, Enchantment enchantment, RegistryOps<JsonElement> ops) {
        if (enchantmentKey == Enchantments.INFINITY)
            return modifyInfinity(enchantment);
        if (enchantmentKey == Enchantments.THORNS)
            return modifyThorns(enchantment);
        return enchantment;
    }

    public static Enchantment modifyInfinity(Enchantment enchantment) {
        DataComponentMap.Builder builder = createDataComponentMapFromEnchantment(enchantment);
        builder.set(EnchiridionEnchantmentEffectComponents.ALLOW_FIRING_WITHOUT_PROJECTILE, List.of(new ConditionalEffect<>(Unit.INSTANCE, Optional.empty())));
        return new Enchantment(enchantment.description(), enchantment.definition(), enchantment.exclusiveSet(), builder.build());
    }

    public static Enchantment modifyThorns(Enchantment enchantment) {
        List<TargetedConditionalEffect<EnchantmentEntityEffect>> newEffects = new ArrayList<>(enchantment.getEffects(EnchantmentEffectComponents.POST_ATTACK));
        Optional<TargetedConditionalEffect<EnchantmentEntityEffect>> originalAllOf = enchantment.getEffects(EnchantmentEffectComponents.POST_ATTACK).stream().filter(effect -> effect.effect() instanceof AllOf.EntityEffects).findFirst();
        AllOf.EntityEffects newAllOf;
        if (originalAllOf.isPresent()) {
            newEffects.remove(originalAllOf.get());
            newAllOf = new AllOf.EntityEffects(new ArrayList<>(((AllOf.EntityEffects)originalAllOf.get().effect()).effects()));
            newAllOf.effects().removeIf(effect -> effect instanceof DamageItem);
            TargetedConditionalEffect<EnchantmentEntityEffect> originalEffect = enchantment.getEffects(EnchantmentEffectComponents.POST_ATTACK).getFirst();
            newEffects.addFirst(new TargetedConditionalEffect<>(originalEffect.enchanted(), originalEffect.affected(), newAllOf,
                    Optional.of(LootItemEntityPropertyCondition.hasProperties(
                            LootContext.EntityTarget.ATTACKER,
                                    EntityPredicate.Builder.entity().distance(DistancePredicate.absolute(MinMaxBounds.Doubles.atMost(2.5F)))
                                    .build())
                            .build())
            ));

        }

        // We'd prefer an enchantment that does *something* rather than one that does nothing.
        if (newEffects.isEmpty())
            return enchantment;

        DataComponentMap.Builder builder = DataComponentMap.builder();
        builder.set(EnchantmentEffectComponents.POST_ATTACK, newEffects);
        return new Enchantment(enchantment.description(), enchantment.definition(), enchantment.exclusiveSet(), builder.build());
    }

    public static DataComponentMap.Builder createDataComponentMapFromEnchantment(Enchantment enchantment) {
        DataComponentMap.Builder builder = DataComponentMap.builder();
        enchantment.effects().forEach(typedDataComponent -> builder.set((DataComponentType<Object>) typedDataComponent.type(), typedDataComponent.value()));
        return builder;
    }
}
