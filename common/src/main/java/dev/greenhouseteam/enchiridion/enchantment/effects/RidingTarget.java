package dev.greenhouseteam.enchiridion.enchantment.effects;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public enum RidingTarget implements StringRepresentable {
    THIS("this", List::of),
    PASSENGER("passenger", entity -> entity.getFirstPassenger() == null ? List.of() : List.of(entity.getFirstPassenger())),
    ALL_PASSENGERS("passengers", Entity::getPassengers),
    VEHICLE("vehicle", entity -> entity.getVehicle() == null ? List.of() : List.of(entity.getVehicle()));

    public static final Codec<RidingTarget> CODEC = StringRepresentable.fromEnum(RidingTarget::values);

    private final String name;
    private final Function<Entity, List<Entity>> getter;

    RidingTarget(String name, Function<Entity, List<Entity>> getter) {
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