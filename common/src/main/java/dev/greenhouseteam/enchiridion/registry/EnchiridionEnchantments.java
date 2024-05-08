package dev.greenhouseteam.enchiridion.registry;

import dev.greenhouseteam.enchiridion.Enchiridion;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.Ignite;

import java.util.UUID;

public class EnchiridionEnchantments {
    public static final ResourceKey<Enchantment> REACH = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("reach"));

    public static final ResourceKey<Enchantment> ASHES_CURSE = ResourceKey.create(Registries.ENCHANTMENT, Enchiridion.asResource("ashes_curse"));

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        HolderSet<Item> ashesEnchantable = items.getOrThrow(Enchiridion.ItemTags.ASHES_ENCHANTABLE);
        HolderSet<Item> miningEnchantable = items.getOrThrow(ItemTags.MINING_ENCHANTABLE);

        Enchantment ashesCurse = Enchantment.enchantment(
                Enchantment.definition(
                        ashesEnchantable, 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8, EquipmentSlotGroup.MAINHAND))
                .withEffect(EnchiridionEnchantmentEffectComponents.POST_BLOCK_DROP, new Ignite(LevelBasedValue.constant(5.0F)))
                .withEffect(EnchiridionEnchantmentEffectComponents.POST_ENTITY_DROP, new Ignite(LevelBasedValue.constant(5.0F)))
                .build(EnchiridionEnchantments.ASHES_CURSE.location());
        Enchantment reach = Enchantment.enchantment(
                Enchantment.definition(
                        miningEnchantable, 1, 2, Enchantment.dynamicCost(12, 5), Enchantment.constantCost(40), 2, EquipmentSlotGroup.MAINHAND))
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES, new EnchantmentAttributeEffect("enchantment.enchiridion.reach", Attributes.BLOCK_INTERACTION_RANGE, LevelBasedValue.perLevel(1.5F, 1.0F), AttributeModifier.Operation.ADD_VALUE, UUID.fromString("164c937c-f04c-4730-b8e9-d299a3a187fa")))
                .build(EnchiridionEnchantments.REACH.location());

        context.register(EnchiridionEnchantments.ASHES_CURSE, ashesCurse);
        context.register(EnchiridionEnchantments.REACH, reach);
    }
}
