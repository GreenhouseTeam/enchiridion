package dev.greenhouseteam.enchiridion.registry;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.enchantment.effects.ExtinguishEffect;
import dev.greenhouseteam.enchiridion.enchantment.effects.FreezeEffect;
import dev.greenhouseteam.enchiridion.enchantment.effects.PreventHungerConsumptionEffect;
import dev.greenhouseteam.enchiridion.enchantment.effects.RidingEntityEffect;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.AllOf;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.Ignite;
import net.minecraft.world.item.enchantment.effects.SpawnParticlesEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.UUID;

public class EnchiridionEnchantments {
    public static final ResourceKey<Enchantment> BARDING = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("barding"));
    public static final ResourceKey<Enchantment> CRUMBLE = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("crumble"));
    public static final ResourceKey<Enchantment> EXHILARATING = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("exhilarating"));
    public static final ResourceKey<Enchantment> ICE_STRIKE = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("ice_strike"));
    public static final ResourceKey<Enchantment> REACH = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("reach"));

    public static final ResourceKey<Enchantment> ASHES_CURSE = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("ashes_curse"));

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);

        HolderSet<Item> legArmorEnchantable = items.getOrThrow(ItemTags.LEG_ARMOR_ENCHANTABLE);
        HolderSet<Item> miningEnchantable = items.getOrThrow(ItemTags.MINING_ENCHANTABLE);
        HolderSet<Item> ashesEnchantable = items.getOrThrow(Enchiridion.ItemTags.ASHES_ENCHANTABLE);
        HolderSet<Item> pickaxeEnchantable = items.getOrThrow(Enchiridion.ItemTags.PICKAXE_ENCHANTABLE);
        HolderSet<Item> iceStrikeEnchantable = items.getOrThrow(Enchiridion.ItemTags.ICE_STRIKE_ENCHANTABLE);
        HolderSet<Item> iceStrikePrimaryEnchantable = items.getOrThrow(Enchiridion.ItemTags.ICE_STRIKE_PRIMARY_ENCHANTABLE);

        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);

        HolderSet<Enchantment> miningExclusiveSet = enchantments.getOrThrow(EnchantmentTags.MINING_EXCLUSIVE);
        HolderSet<Enchantment> elementalExclusiveSet = enchantments.getOrThrow(Enchiridion.EnchantmentTags.ELEMENTAL_EXCLUSIVE);

        Enchantment ashesCurse = Enchantment.enchantment(
                Enchantment.definition(
                        ashesEnchantable, 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8, EquipmentSlotGroup.MAINHAND)
                ).withEffect(EnchiridionEnchantmentEffectComponents.POST_BLOCK_DROP, new Ignite(LevelBasedValue.constant(5.0F)))
                .withEffect(EnchiridionEnchantmentEffectComponents.POST_ENTITY_DROP, new Ignite(LevelBasedValue.constant(5.0F)))
                .build(ASHES_CURSE.location());
        Enchantment barding = Enchantment.enchantment(
                Enchantment.definition(
                        legArmorEnchantable, 5, 4, Enchantment.dynamicCost(5, 6), Enchantment.dynamicCost(11, 6), 2, EquipmentSlotGroup.ARMOR)
                )
                .withEffect(EnchiridionEnchantmentEffectComponents.VEHICLE_DAMAGE_PROTECTION, new AddValue(LevelBasedValue.perLevel(2.0F)),
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY)))
                                .and(InvertedLootItemCondition.invert(
                                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                                                .vehicle(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(Enchiridion.EntityTypeTags.IGNORES_BARDING)))))))
                .withEffect(EnchantmentEffectComponents.TICK, new RidingEntityEffect(RidingEntityEffect.Target.VEHICLE, new SpawnParticlesEffect(ParticleTypes.END_ROD, SpawnParticlesEffect.inBoundingBox(), SpawnParticlesEffect.inBoundingBox(), SpawnParticlesEffect.fixedVelocity(UniformFloat.of(-0.1F, 0.1F)), SpawnParticlesEffect.fixedVelocity(UniformFloat.of(-0.1F, 0.1F)), ConstantFloat.of(1.0F))),
                        LootItemEntityPropertyCondition.hasProperties(
                                LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                                .periodicTick(5))
                                .and(InvertedLootItemCondition.invert(
                                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                                                .vehicle(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(Enchiridion.EntityTypeTags.IGNORES_BARDING)))))))
                .build(BARDING.location());
        Enchantment crumble = Enchantment.enchantment(
                        Enchantment.definition(
                                pickaxeEnchantable, 2, 1, Enchantment.constantCost(33), Enchantment.constantCost(83), 4, EquipmentSlotGroup.MAINHAND)
                ).exclusiveWith(miningExclusiveSet)
                .withEffect(EnchiridionEnchantmentEffectComponents.TARGET_BLOCK_CHANGED, new EnchantmentAttributeEffect("enchantment.enchiridion.crumble", Attributes.BLOCK_BREAK_SPEED, LevelBasedValue.constant(1.0F), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, UUID.fromString("031e1965-9647-4271-8bf2-7aecdd20ab09")),
                        LocationCheck.checkLocation(
                                LocationPredicate.Builder.location()
                                .setBlock(
                                        net.minecraft.advancements.critereon.BlockPredicate.Builder.block()
                                                .of(Enchiridion.BlockTags.BASE_STONE)))
                ).build(CRUMBLE.location());
        Enchantment exhilarating = Enchantment.enchantment(
                        Enchantment.definition(
                                miningEnchantable, 1, 1, Enchantment.dynamicCost(12, 4), Enchantment.constantCost(35), 1, EquipmentSlotGroup.MAINHAND)
                ).withEffect(EnchiridionEnchantmentEffectComponents.PREVENT_HUNGER_CONSUMPTION, new PreventHungerConsumptionEffect(false, true, false))
                .build(EXHILARATING.location());
        Enchantment iceStrike = Enchantment.enchantment(
                Enchantment.definition(
                        iceStrikeEnchantable, iceStrikePrimaryEnchantable, 2, 2, Enchantment.dynamicCost(10, 20), Enchantment.dynamicCost(60, 20), 4, EquipmentSlotGroup.MAINHAND)
                ).exclusiveWith(elementalExclusiveSet)
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, AllOf.entityEffects(ExtinguishEffect.INSTANCE, new FreezeEffect(LevelBasedValue.perLevel(300F, 160F))),
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().isDirect(true)))
                .withEffect(EnchiridionEnchantmentEffectComponents.POST_SHIELD_DISABLE, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, AllOf.entityEffects(ExtinguishEffect.INSTANCE, new FreezeEffect(LevelBasedValue.perLevel(460F, 160F))),
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().isDirect(true)))
                .build(ICE_STRIKE.location());
        Enchantment reach = Enchantment.enchantment(
                Enchantment.definition(
                        miningEnchantable, 1, 2, Enchantment.dynamicCost(12, 7), Enchantment.constantCost(50), 2, EquipmentSlotGroup.MAINHAND)
                ).withEffect(EnchantmentEffectComponents.ATTRIBUTES, new EnchantmentAttributeEffect("enchantment.enchiridion.reach", Attributes.BLOCK_INTERACTION_RANGE, LevelBasedValue.perLevel(0.5F, 0.5F), AttributeModifier.Operation.ADD_VALUE, UUID.fromString("164c937c-f04c-4730-b8e9-d299a3a187fa")))
                .build(REACH.location());

        context.register(ASHES_CURSE, ashesCurse);
        context.register(BARDING, barding);
        context.register(CRUMBLE, crumble);
        context.register(EXHILARATING, exhilarating);
        context.register(ICE_STRIKE, iceStrike);
        context.register(REACH, reach);
    }
}
