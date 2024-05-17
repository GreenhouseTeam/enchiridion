package dev.greenhouseteam.enchiridion.access;

import dev.greenhouseteam.enchiridion.enchantment.effects.RidingConditionalEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;

public interface EntityRidingEffectsAccess {
    void enchiridion$addRidingEffect(EquipmentSlot slot, int level, RidingConditionalEffect<EnchantmentLocationBasedEffect> effect);
    void enchiridion$resetRidingEffects();
}
