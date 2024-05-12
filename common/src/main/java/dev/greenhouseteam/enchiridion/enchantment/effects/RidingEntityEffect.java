package dev.greenhouseteam.enchiridion.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record RidingEntityEffect(Target target, EnchantmentEntityEffect effect, Optional<EntityPredicate> predicate) implements EnchantmentEntityEffect {
    public static final MapCodec<RidingEntityEffect> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Target.CODEC.fieldOf("target").forGetter(RidingEntityEffect::target),
            EnchantmentEntityEffect.CODEC.fieldOf("effect").forGetter(RidingEntityEffect::effect),
            EntityPredicate.CODEC.optionalFieldOf("predicate").forGetter(RidingEntityEffect::predicate)
    ).apply(inst, RidingEntityEffect::new));

    public RidingEntityEffect(Target target, EnchantmentEntityEffect effect) {
        this(target, effect, Optional.empty());
    }

    public RidingEntityEffect(Target target, EnchantmentEntityEffect effect, EntityPredicate predicate) {
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

    public enum Target implements StringRepresentable {
        PASSENGER("passenger", entity -> entity.getFirstPassenger() == null ? List.of() : List.of(entity.getFirstPassenger())),
        ALL_PASSENGERS("passengers", Entity::getPassengers),
        VEHICLE("vehicle", entity -> entity.getVehicle() == null ? List.of() : List.of(entity.getVehicle()));

        public static final Codec<Target> CODEC = StringRepresentable.fromEnum(Target::values);

        private final String name;
        private final Function<Entity, List<Entity>> getter;

        Target(String name, Function<Entity, List<Entity>> getter) {
            this.name = name;
            this.getter = getter;
        }

        public List<Entity> getEntities(Entity entity) {
            return getter.apply(entity);
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
