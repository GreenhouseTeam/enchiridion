package dev.greenhouseteam.enchiridion.enchantment.effects;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public class ExtinguishEffect implements EnchantmentEntityEffect {
    public static final ExtinguishEffect INSTANCE = new ExtinguishEffect();
    public static final MapCodec<ExtinguishEffect> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public void apply(ServerLevel serverLevel, int level, EnchantedItemInUse enchantedItem, Entity entity, Vec3 origin) {
        entity.extinguishFire();
    }

    @Override
    public MapCodec<ExtinguishEffect> codec() {
        return CODEC;
    }
}
