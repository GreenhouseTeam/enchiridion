package dev.greenhouseteam.enchiridion.registry;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.enchantment.effects.ExtinguishEffect;
import dev.greenhouseteam.enchiridion.enchantment.effects.FreezeEntityEffect;
import dev.greenhouseteam.enchiridion.enchantment.effects.PreventHungerConsumptionEffect;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.Ignite;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;

import java.util.UUID;

public class EnchiridionEnchantments {
    public static final ResourceKey<Enchantment> EXHILARATING = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("exhilarating"));
    public static final ResourceKey<Enchantment> ICE_CRUSH = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("ice_crush"));
    public static final ResourceKey<Enchantment> REACH = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("reach"));

    public static final ResourceKey<Enchantment> ASHES_CURSE = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("ashes_curse"));

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        HolderSet<Item> ashesEnchantable = items.getOrThrow(Enchiridion.ItemTags.ASHES_ENCHANTABLE);
        HolderSet<Item> iceCrushEnchantable = items.getOrThrow(Enchiridion.ItemTags.ICE_CRUSH_ENCHANTABLE);
        HolderSet<Item> iceCrushPrimaryEnchantable = items.getOrThrow(Enchiridion.ItemTags.ICE_CRUSH_PRIMARY_ENCHANTABLE);
        HolderSet<Item> miningEnchantable = items.getOrThrow(ItemTags.MINING_ENCHANTABLE);

        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);
        HolderSet<Enchantment> elementExclusiveSet = enchantments.getOrThrow(Enchiridion.EnchantmentTags.ELEMENTAL_EXCLUSIVE);

        Enchantment ashesCurse = Enchantment.enchantment(
                Enchantment.definition(
                        ashesEnchantable, 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8, EquipmentSlotGroup.MAINHAND))
                .withEffect(EnchiridionEnchantmentEffectComponents.POST_BLOCK_DROP, new Ignite(LevelBasedValue.constant(5.0F)))
                .withEffect(EnchiridionEnchantmentEffectComponents.POST_ENTITY_DROP, new Ignite(LevelBasedValue.constant(5.0F)))
                .build(EnchiridionEnchantments.ASHES_CURSE.location());
        Enchantment iceCrush = Enchantment.enchantment(
                Enchantment.definition(
                        iceCrushEnchantable, iceCrushPrimaryEnchantable, 2, 2, Enchantment.dynamicCost(10, 20), Enchantment.dynamicCost(60, 20), 4, EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(elementExclusiveSet)
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, ExtinguishEffect.INSTANCE,
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().isDirect(true)))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, new FreezeEntityEffect(LevelBasedValue.perLevel(300F, 160F)),
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().isDirect(true)))
                .withEffect(EnchiridionEnchantmentEffectComponents.POST_SHIELD_DISABLE, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, new FreezeEntityEffect(LevelBasedValue.perLevel(460F, 160F)),
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().isDirect(true)))
                .build(EnchiridionEnchantments.ICE_CRUSH.location());
        Enchantment reach = Enchantment.enchantment(
                Enchantment.definition(
                        miningEnchantable, 1, 2, Enchantment.dynamicCost(12, 7), Enchantment.constantCost(50), 2, EquipmentSlotGroup.MAINHAND))
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES, new EnchantmentAttributeEffect("enchantment.enchiridion.reach", Attributes.BLOCK_INTERACTION_RANGE, LevelBasedValue.perLevel(0.5F, 0.5F), AttributeModifier.Operation.ADD_VALUE, UUID.fromString("164c937c-f04c-4730-b8e9-d299a3a187fa")))
                .build(EnchiridionEnchantments.REACH.location());
        Enchantment exhilarating = Enchantment.enchantment(
                Enchantment.definition(
                        miningEnchantable, 1, 1, Enchantment.dynamicCost(12, 4), Enchantment.constantCost(35), 1, EquipmentSlotGroup.MAINHAND))
                .withEffect(EnchiridionEnchantmentEffectComponents.PREVENT_HUNGER_CONSUMPTION, new PreventHungerConsumptionEffect(false, true, false))
                .build(EnchiridionEnchantments.EXHILARATING.location());

        context.register(EnchiridionEnchantments.ASHES_CURSE, ashesCurse);
        context.register(EnchiridionEnchantments.EXHILARATING, exhilarating);
        context.register(EnchiridionEnchantments.ICE_CRUSH, iceCrush);
        context.register(EnchiridionEnchantments.REACH, reach);
    }
}
