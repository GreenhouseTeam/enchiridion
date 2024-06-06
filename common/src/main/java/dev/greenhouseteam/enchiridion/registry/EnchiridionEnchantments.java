package dev.greenhouseteam.enchiridion.registry;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.enchantment.effects.ExtinguishEffect;
import dev.greenhouseteam.enchiridion.enchantment.effects.FreezeEffect;
import dev.greenhouseteam.enchiridion.enchantment.effects.PreventHungerConsumptionEffect;
import dev.greenhouseteam.enchiridion.enchantment.effects.RidingConditionalEffect;
import dev.greenhouseteam.enchiridion.enchantment.effects.RidingEntityEffect;
import dev.greenhouseteam.enchiridion.enchantment.effects.RidingTarget;
import dev.greenhouseteam.enchiridion.enchantment.effects.RunFunctionOnLootEffect;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.item.enchantment.effects.DamageEntity;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.Ignite;
import net.minecraft.world.item.enchantment.effects.MultiplyValue;
import net.minecraft.world.item.enchantment.effects.SpawnParticlesEffect;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.EnchantmentLevelProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EnchiridionEnchantments {
    public static final ResourceKey<Enchantment> BARDING = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("barding"));
    public static final ResourceKey<Enchantment> CRUMBLE = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("crumble"));
    public static final ResourceKey<Enchantment> DREDGE = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("dredge"));
    public static final ResourceKey<Enchantment> EXHILARATING = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("exhilarating"));
    public static final ResourceKey<Enchantment> EXPERIENCED = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("experienced"));
    public static final ResourceKey<Enchantment> FORECAST = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("forecast"));
    public static final ResourceKey<Enchantment> ICE_STRIKE = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("ice_strike"));
    public static final ResourceKey<Enchantment> JOUSTING = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("jousting"));
    public static final ResourceKey<Enchantment> REACH = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("reach"));
    public static final ResourceKey<Enchantment> RELIABLE = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("reliable"));

    public static final ResourceKey<Enchantment> ASHES_CURSE = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("ashes_curse"));

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);

        HolderSet<Item> legArmorEnchantable = items.getOrThrow(ItemTags.LEG_ARMOR_ENCHANTABLE);
        HolderSet<Item> fishingEnchantable = items.getOrThrow(ItemTags.FISHING_ENCHANTABLE);
        HolderSet<Item> miningEnchantable = items.getOrThrow(ItemTags.MINING_ENCHANTABLE);
        HolderSet<Item> swordEnchantable = items.getOrThrow(ItemTags.SWORD_ENCHANTABLE);

        HolderSet<Item> ashesEnchantable = items.getOrThrow(Enchiridion.ItemTags.ASHES_ENCHANTABLE);
        HolderSet<Item> iceStrikeEnchantable = items.getOrThrow(Enchiridion.ItemTags.ICE_STRIKE_ENCHANTABLE);
        HolderSet<Item> iceStrikePrimaryEnchantable = items.getOrThrow(Enchiridion.ItemTags.ICE_STRIKE_PRIMARY_ENCHANTABLE);
        HolderSet<Item> pickaxeEnchantable = items.getOrThrow(Enchiridion.ItemTags.PICKAXE_ENCHANTABLE);

        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);

        HolderSet<Enchantment> miningExclusiveSet = enchantments.getOrThrow(EnchantmentTags.MINING_EXCLUSIVE);
        HolderSet<Enchantment> elementalExclusiveSet = enchantments.getOrThrow(Enchiridion.EnchantmentTags.ELEMENTAL_EXCLUSIVE);
        HolderSet<Enchantment> fishingExclusiveSet = enchantments.getOrThrow(Enchiridion.EnchantmentTags.FISHING_EXCLUSIVE);

        HolderGetter<DamageType> damageType = context.lookup(Registries.DAMAGE_TYPE);
        Holder<DamageType> freeze = damageType.getOrThrow(DamageTypes.FREEZE);

        Enchantment ashesCurse = Enchantment.enchantment(
                Enchantment.definition(ashesEnchantable, 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8, EquipmentSlotGroup.MAINHAND)
                ).withEffect(EnchiridionEnchantmentEffectComponents.POST_BLOCK_DROP, new Ignite(LevelBasedValue.constant(5.0F)))
                .withEffect(EnchiridionEnchantmentEffectComponents.POST_ENTITY_DROP, new Ignite(LevelBasedValue.constant(5.0F)))
                .build(ASHES_CURSE.location());
        Enchantment barding = Enchantment.enchantment(
                Enchantment.definition(legArmorEnchantable, 5, 4, Enchantment.dynamicCost(5, 6), Enchantment.dynamicCost(11, 6), 2, EquipmentSlotGroup.ARMOR)
                ).withEffect(EnchiridionEnchantmentEffectComponents.VEHICLE_DAMAGE_PROTECTION, new AddValue(LevelBasedValue.perLevel(1.0F)),
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY)))
                                .and(InvertedLootItemCondition.invert(
                                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                                                .vehicle(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(Enchiridion.EntityTypeTags.IGNORES_BARDING)))))))
                .withEffect(EnchiridionEnchantmentEffectComponents.VEHICLE_DAMAGE_PROTECTION, new AddValue(LevelBasedValue.perLevel(2.0F)),
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY)))
                                .and(InvertedLootItemCondition.invert(
                                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                                                .vehicle(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(Enchiridion.EntityTypeTags.IGNORES_BARDING))))
                                                .or(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                                                        .vehicle(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.PLAYER)))))))
                                .and(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                                        .vehicle(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.PLAYER))))))
                .withEffect(EnchantmentEffectComponents.TICK, new RidingEntityEffect(RidingTarget.VEHICLE, new SpawnParticlesEffect(ParticleTypes.END_ROD, SpawnParticlesEffect.inBoundingBox(), SpawnParticlesEffect.inBoundingBox(), SpawnParticlesEffect.fixedVelocity(UniformFloat.of(-0.1F, 0.1F)), SpawnParticlesEffect.fixedVelocity(UniformFloat.of(-0.1F, 0.1F)), ConstantFloat.of(1.0F))),
                        LootItemEntityPropertyCondition.hasProperties(
                                LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                                .periodicTick(5))
                                .and(InvertedLootItemCondition.invert(
                                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                                                .vehicle(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(Enchiridion.EntityTypeTags.IGNORES_BARDING)))))))
                .build(BARDING.location());
        Enchantment crumble = Enchantment.enchantment(
                Enchantment.definition(pickaxeEnchantable, 2, 1, Enchantment.constantCost(15), Enchantment.constantCost(65), 4, EquipmentSlotGroup.MAINHAND)
                ).exclusiveWith(miningExclusiveSet)
                .withEffect(EnchiridionEnchantmentEffectComponents.TARGET_BLOCK_CHANGED, new EnchantmentAttributeEffect("enchantment.enchiridion.crumble", Attributes.MINING_EFFICIENCY, LevelBasedValue.constant(8.0F), AttributeModifier.Operation.ADD_VALUE, UUID.fromString("031e1965-9647-4271-8bf2-7aecdd20ab09")),
                        AllOfCondition.allOf(
                                LocationCheck.checkLocation(LocationPredicate.Builder.location()
                                        .setBlock(
                                                BlockPredicate.Builder.block()
                                                        .of(Enchiridion.BlockTags.BASE_STONE))),
                                InvertedLootItemCondition.invert(
                                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                                EntityPredicate.Builder.entity()
                                                        .effects(MobEffectsPredicate.Builder.effects()
                                                                .and(MobEffects.DIG_SPEED, new MobEffectsPredicate.MobEffectInstancePredicate())))))
                ).withEffect(EnchiridionEnchantmentEffectComponents.TARGET_BLOCK_CHANGED, new EnchantmentAttributeEffect("enchantment.enchiridion.crumble", Attributes.MINING_EFFICIENCY, LevelBasedValue.constant(16.0F), AttributeModifier.Operation.ADD_VALUE, UUID.fromString("031e1965-9647-4271-8bf2-7aecdd20ab09")),
                        AllOfCondition.allOf(
                                LocationCheck.checkLocation(LocationPredicate.Builder.location()
                                        .setBlock(
                                                BlockPredicate.Builder.block()
                                                        .of(Enchiridion.BlockTags.BASE_STONE))),
                                LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                        EntityPredicate.Builder.entity()
                                                .effects(MobEffectsPredicate.Builder.effects()
                                                        .and(MobEffects.DIG_SPEED, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.exactly(0), MinMaxBounds.Ints.ANY, Optional.empty(), Optional.empty()))))
                        )
                ).withEffect(EnchiridionEnchantmentEffectComponents.TARGET_BLOCK_CHANGED, new EnchantmentAttributeEffect("enchantment.enchiridion.crumble", Attributes.MINING_EFFICIENCY, LevelBasedValue.constant(32.0F), AttributeModifier.Operation.ADD_VALUE, UUID.fromString("031e1965-9647-4271-8bf2-7aecdd20ab09")),
                        AllOfCondition.allOf(
                                LocationCheck.checkLocation(LocationPredicate.Builder.location()
                                        .setBlock(
                                                BlockPredicate.Builder.block()
                                                        .of(Enchiridion.BlockTags.BASE_STONE))),
                                LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                        EntityPredicate.Builder.entity()
                                                .effects(MobEffectsPredicate.Builder.effects()
                                                        .and(MobEffects.DIG_SPEED, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.atLeast(1), MinMaxBounds.Ints.ANY, Optional.empty(), Optional.empty()))))
                        )
                ).withEffect(EnchiridionEnchantmentEffectComponents.TARGET_BLOCK_CHANGED, new EnchantmentAttributeEffect("enchantment.enchiridion.crumble.harder_stone", Attributes.MINING_EFFICIENCY, LevelBasedValue.constant(0.4F), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, UUID.fromString("e2fd9d91-1220-438d-ad67-cc11f499129f")),
                        LocationCheck.checkLocation(LocationPredicate.Builder.location()
                                .setBlock(
                                        BlockPredicate.Builder.block()
                                                .of(Enchiridion.BlockTags.HARDER_STONE)
                                )
                        )
                ).build(CRUMBLE.location());
        Enchantment dredge = Enchantment.enchantment(
                Enchantment.definition(fishingEnchantable, 2, 3, Enchantment.dynamicCost(15, 9), Enchantment.dynamicCost(65, 9), 4, EquipmentSlotGroup.MAINHAND)
                ).exclusiveWith(fishingExclusiveSet)
                .withEffect(EnchiridionEnchantmentEffectComponents.ADDITIONAL_FISHING_LOOT, BuiltInLootTables.FISHING, LootItemRandomChanceCondition.randomChance(EnchantmentLevelProvider.forEnchantmentLevel(LevelBasedValue.perLevel(0.1F))))
                .build(DREDGE.location());
        Enchantment exhilarating = Enchantment.enchantment(
                Enchantment.definition(miningEnchantable, 1, 1, Enchantment.dynamicCost(12, 4), Enchantment.constantCost(35), 1, EquipmentSlotGroup.MAINHAND)
                ).withEffect(EnchiridionEnchantmentEffectComponents.PREVENT_HUNGER_CONSUMPTION, new PreventHungerConsumptionEffect(false, true, false))
                .build(EXHILARATING.location());
        Enchantment experienced = Enchantment.enchantment(
                        Enchantment.definition(fishingEnchantable, 1, 2, Enchantment.dynamicCost(15, 6), Enchantment.dynamicCost(45, 5), 3, EquipmentSlotGroup.MAINHAND)
                ).withEffect(EnchiridionEnchantmentEffectComponents.FISHING_EXPERIENCE_BONUS, new MultiplyValue(LevelBasedValue.perLevel(1.5F, 1.0F)))
                .build(EXPERIENCED.location());
        Enchantment forecast = Enchantment.enchantment(
                        Enchantment.definition(fishingEnchantable, 2, 2, Enchantment.dynamicCost(10, 5), Enchantment.dynamicCost(40, 5), 2, EquipmentSlotGroup.MAINHAND)
                ).withEffect(EnchantmentEffectComponents.FISHING_LUCK_BONUS, new AddValue(LevelBasedValue.perLevel(0.5F, 0.5F)),
                        WeatherCheck.weather().setRaining(true)
                                .and(LootItemEntityPropertyCondition.hasProperties(
                                        LootContext.EntityTarget.THIS,
                                        EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setCanSeeSky(true))))
                ).build(FORECAST.location());
        Enchantment iceStrike = Enchantment.enchantment(
                Enchantment.definition(iceStrikeEnchantable, iceStrikePrimaryEnchantable, 2, 2, Enchantment.dynamicCost(10, 20), Enchantment.dynamicCost(60, 20), 4, EquipmentSlotGroup.MAINHAND)
                ).exclusiveWith(elementalExclusiveSet)
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, AllOf.entityEffects(ExtinguishEffect.INSTANCE, new FreezeEffect(LevelBasedValue.perLevel(300F, 160F))),
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().isDirect(true)))
                .withEffect(EnchiridionEnchantmentEffectComponents.POST_SHIELD_DISABLE, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, AllOf.entityEffects(ExtinguishEffect.INSTANCE, new DamageEntity(LevelBasedValue.perLevel(2.0F), LevelBasedValue.perLevel(2.0F), freeze), new FreezeEffect(LevelBasedValue.perLevel(460F, 160F))),
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().isDirect(true)))
                .build(ICE_STRIKE.location());
        Enchantment jousting = Enchantment.enchantment(Enchantment.definition(swordEnchantable, 2, 3, Enchantment.dynamicCost(10, 10), Enchantment.dynamicCost(25, 10), 4, EquipmentSlotGroup.MAINHAND)
                ).withSpecialEffect(EnchiridionEnchantmentEffectComponents.VEHICLE_CHANGED, List.of(new RidingConditionalEffect<>(
                        RidingTarget.THIS,
                        RidingTarget.THIS,
                        new EnchantmentAttributeEffect("enchantment.enchiridion.jousting", Attributes.ENTITY_INTERACTION_RANGE, LevelBasedValue.perLevel(1.0F, 0.5F), AttributeModifier.Operation.ADD_VALUE, UUID.fromString("d85d7660-c3d6-4d43-b5a0-279e79ab8ff0")),
                        InvertedLootItemCondition.invert(
                                LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                                        .vehicle(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(Enchiridion.EntityTypeTags.PREVENTS_JOUSTING))))).build()))
                ).build(JOUSTING.location());
        Enchantment reach = Enchantment.enchantment(
                Enchantment.definition(miningEnchantable, 1, 2, Enchantment.dynamicCost(12, 7), Enchantment.constantCost(50), 2, EquipmentSlotGroup.MAINHAND)
                ).withEffect(EnchantmentEffectComponents.ATTRIBUTES, new EnchantmentAttributeEffect("enchantment.enchiridion.reach", Attributes.BLOCK_INTERACTION_RANGE, LevelBasedValue.perLevel(0.5F, 0.5F), AttributeModifier.Operation.ADD_VALUE, UUID.fromString("164c937c-f04c-4730-b8e9-d299a3a187fa")))
                .build(REACH.location());
        Enchantment reliable = Enchantment.enchantment(
                        Enchantment.definition(fishingEnchantable, 2, 1, Enchantment.constantCost(5), Enchantment.constantCost(12), 2, EquipmentSlotGroup.MAINHAND)
                ).withEffect(EnchiridionEnchantmentEffectComponents.RUN_FUNCTIONS_ON_FISHING_LOOT, List.of(
                        new RunFunctionOnLootEffect(List.of(
                                SetItemDamageFunction.setDamage(UniformGenerator.between(0.15F, 0.4F), true).build()
                        ), ItemPredicate.Builder.item().of(ItemTags.DURABILITY_ENCHANTABLE).build()),
                        new RunFunctionOnLootEffect(List.of(
                                new SetEnchantmentsFunction.Builder().build(),
                                new EnchantWithLevelsFunction.Builder(ConstantValue.exactly(35.0F)).fromOptions(enchantments.getOrThrow(EnchantmentTags.ON_RANDOM_LOOT)).build()
                        ), ItemPredicate.Builder.item().of(Enchiridion.ItemTags.INCLUSIVE_ENCHANTABLES).build(), BuiltInLootTables.FISHING_TREASURE)
                ))
                .build(RELIABLE.location());

        context.register(ASHES_CURSE, ashesCurse);
        context.register(BARDING, barding);
        context.register(CRUMBLE, crumble);
        context.register(DREDGE, dredge);
        context.register(EXHILARATING, exhilarating);
        context.register(EXPERIENCED, experienced);
        context.register(FORECAST, forecast);
        context.register(ICE_STRIKE, iceStrike);
        context.register(JOUSTING, jousting);
        context.register(REACH, reach);
        context.register(RELIABLE, reliable);
    }
}
