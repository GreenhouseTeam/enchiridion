package dev.greenhouseteam.enchiridion.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.enchiridion.Enchiridion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record FreezeEntityEffect(LevelBasedValue duration) implements EnchantmentEntityEffect {
    public static final MapCodec<FreezeEntityEffect> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            LevelBasedValue.CODEC.fieldOf("duration").forGetter(FreezeEntityEffect::duration)
    ).apply(inst, FreezeEntityEffect::new));


    @Override
    public void apply(ServerLevel serverLevel, int level, EnchantedItemInUse enchantedItem, Entity entity, Vec3 origin) {
        int i = Mth.floor(this.duration.calculate(level));
        if (i < entity.getTicksFrozen())
            return;
        entity.setTicksFrozen(i);
        Enchiridion.getHelper().setFrozenByEnchantment(entity, true);
    }

    @Override
    public MapCodec<FreezeEntityEffect> codec() {
        return CODEC;
    }
}
