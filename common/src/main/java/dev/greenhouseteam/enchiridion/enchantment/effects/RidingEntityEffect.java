package dev.greenhouseteam.enchiridion.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public record RidingEntityEffect(RidingTarget target, EnchantmentEntityEffect effect, Optional<EntityPredicate> predicate) implements EnchantmentEntityEffect {
    public static final MapCodec<RidingEntityEffect> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            RidingTarget.CODEC.fieldOf("target").forGetter(RidingEntityEffect::target),
            EnchantmentEntityEffect.CODEC.fieldOf("effect").forGetter(RidingEntityEffect::effect),
            EntityPredicate.CODEC.optionalFieldOf("predicate").forGetter(RidingEntityEffect::predicate)
    ).apply(inst, RidingEntityEffect::new));

    public RidingEntityEffect(RidingTarget target, EnchantmentEntityEffect effect) {
        this(target, effect, Optional.empty());
    }

    public RidingEntityEffect(RidingTarget target, EnchantmentEntityEffect effect, EntityPredicate predicate) {
        this(target, effect, Optional.of(predicate));
    }

    @Override
    public void apply(ServerLevel serverLevel, int level, EnchantedItemInUse enchantedItem, Entity entity, Vec3 origin) {
        List<Entity> entities = target.getEntities(entity);
        for (Entity target : entities) {
            if (predicate.isEmpty() || predicate.get().matches(serverLevel, target.position(), target))
                effect.apply(serverLevel, level, enchantedItem, target, target.position());
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
