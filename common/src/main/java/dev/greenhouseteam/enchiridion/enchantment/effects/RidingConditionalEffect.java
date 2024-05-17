package dev.greenhouseteam.enchiridion.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public record RidingConditionalEffect<T extends EnchantmentLocationBasedEffect>(RidingTarget enchanted, RidingTarget affected, T effect, Optional<LootItemCondition> requirements) {
    public static <S extends EnchantmentLocationBasedEffect> Codec<RidingConditionalEffect<S>> codec(Codec<S> effectCodec, LootContextParamSet paramSet) {
        return RecordCodecBuilder.create(inst -> inst.group(
                RidingTarget.CODEC.fieldOf("enchanted").forGetter(RidingConditionalEffect::enchanted),
                RidingTarget.CODEC.fieldOf("affected").forGetter(RidingConditionalEffect::affected),
                effectCodec.fieldOf("effect").forGetter(RidingConditionalEffect::effect),
                ConditionalEffect.conditionCodec(paramSet).optionalFieldOf("requirements").forGetter(RidingConditionalEffect::requirements)
        ).apply(inst, RidingConditionalEffect::new));
    }

    public RidingConditionalEffect(RidingTarget enchanted, RidingTarget affected, T effect, LootItemCondition requirements) {
        this(enchanted, affected, effect, Optional.of(requirements));
    }

    public boolean matches(LootContext context) {
        return this.requirements.map(condition -> condition.test(context)).orElse(true);
    }
}
