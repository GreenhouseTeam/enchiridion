package dev.greenhouseteam.enchiridion.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Optional;

public record PreventHungerConsumptionEffect(boolean sprinting, boolean mining, boolean jumping) {
    public static final Codec<PreventHungerConsumptionEffect> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.optionalFieldOf("sprinting", false).forGetter(PreventHungerConsumptionEffect::sprinting),
            Codec.BOOL.optionalFieldOf("mining", false).forGetter(PreventHungerConsumptionEffect::mining),
            Codec.BOOL.optionalFieldOf("jumping", false).forGetter(PreventHungerConsumptionEffect::jumping)
    ).apply(inst, PreventHungerConsumptionEffect::new));

    public static boolean shouldPreventSprintingConsumption(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            LootParams.Builder params = new LootParams.Builder(serverLevel).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ORIGIN, entity.position());
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                boolean bl = entity.getItemBySlot(slot).getEnchantments().entrySet().stream().anyMatch(entry -> entry.getKey().isBound() && entry.getKey().value().matchingSlot(slot) && entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.PREVENT_HUNGER_CONSUMPTION).stream().anyMatch(effect -> {
                    if (!effect.effect().jumping())
                        return false;
                    params.withParameter(LootContextParams.ENCHANTMENT_LEVEL, entry.getIntValue());
                    return effect.matches(new LootContext.Builder(params.create(LootContextParamSets.ENCHANTED_ENTITY)).create(Optional.empty()));
                }));
                if (bl)
                    return true;
            }
        }
        return false;
    }

    public static boolean shouldPreventMiningConsumption(ItemStack stack, Player player, BlockPos blockPos) {
        if (player.level() instanceof ServerLevel serverLevel) {
            LootParams.Builder params = new LootParams.Builder(serverLevel).withParameter(LootContextParams.THIS_ENTITY, player).withParameter(LootContextParams.ORIGIN, blockPos.getCenter());
            return stack.getEnchantments().entrySet().stream().anyMatch(entry -> entry.getKey().isBound() && entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.PREVENT_HUNGER_CONSUMPTION).stream().anyMatch(effect -> {
                if (!effect.effect().mining())
                    return false;
                params.withParameter(LootContextParams.ENCHANTMENT_LEVEL, entry.getIntValue());
                return effect.matches(new LootContext.Builder(params.create(LootContextParamSets.ENCHANTED_ENTITY)).create(Optional.empty()));
            }));
        }
        return false;
    }

    public static boolean shouldPreventJumpingConsumption(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            LootParams.Builder params = new LootParams.Builder(serverLevel).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ORIGIN, entity.position());
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                boolean bl = entity.getItemBySlot(slot).getEnchantments().entrySet().stream().anyMatch(entry -> entry.getKey().isBound() && entry.getKey().value().matchingSlot(slot) && entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.PREVENT_HUNGER_CONSUMPTION).stream().anyMatch(effect -> {
                    if (!effect.effect().jumping())
                        return false;
                    params.withParameter(LootContextParams.ENCHANTMENT_LEVEL, entry.getIntValue());
                    return effect.matches(new LootContext.Builder(params.create(LootContextParamSets.ENCHANTED_ENTITY)).create(Optional.empty()));
                }));
                if (bl)
                    return true;
            }
        }
        return false;
    }
}
