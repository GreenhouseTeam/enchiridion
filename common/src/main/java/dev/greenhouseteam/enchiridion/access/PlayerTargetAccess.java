package dev.greenhouseteam.enchiridion.access;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;

import java.util.Map;
import java.util.Set;

public interface PlayerTargetAccess {
    Map<Holder<Enchantment>, Set<EnchantmentLocationBasedEffect>> enchiridion$activeBlockTargetEnchantmentEffects();
    void enchiridion$addActiveBlockTargetEnchantmentEffect(Holder<Enchantment> enchantment, EnchantmentLocationBasedEffect effect);
    void enchiridion$removeActiveBlockTargetEnchantmentEffect(Holder<Enchantment> enchantment, EnchantmentLocationBasedEffect effect);
}
