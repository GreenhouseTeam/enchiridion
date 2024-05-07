package dev.greenhouseteam.enchiridion.datagen;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantments;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
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
import java.util.concurrent.CompletableFuture;

public class EnchiridionDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(EnchantmentProvider::new);
        pack.addProvider(EnchantmentTagProvider::new);
        pack.addProvider(ItemTagProvider::new);
    }

    public static class EnchantmentProvider extends FabricDynamicRegistryProvider {
        public EnchantmentProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void configure(HolderLookup.Provider registries, Entries entries) {
            HolderLookup<Item> items = registries.lookupOrThrow(Registries.ITEM);
            HolderSet<Item> miningEnchantable = items.getOrThrow(ItemTags.MINING_ENCHANTABLE);
            HolderSet<Item> ashesEnchantable = items.getOrThrow(Enchiridion.ItemTags.ASHES_ENCHANTABLE);

            Enchantment ashesCurse = Enchantment.enchantment(
                    Enchantment.definition(
                            ashesEnchantable, 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8, EquipmentSlotGroup.MAINHAND))
                    .withEffect(EnchiridionEnchantmentEffectComponents.POST_EQUIPMENT_DROPS, new Ignite(LevelBasedValue.constant(5.0F)))
                    .build(EnchiridionEnchantments.ASHES_CURSE.location());
            Enchantment reach = Enchantment.enchantment(
                    Enchantment.definition(
                            miningEnchantable, 1, 2, Enchantment.dynamicCost(12, 5), Enchantment.constantCost(40), 2, EquipmentSlotGroup.MAINHAND))
                    .withEffect(EnchantmentEffectComponents.ATTRIBUTES, new EnchantmentAttributeEffect("enchantment.enchiridion.reach", Attributes.BLOCK_INTERACTION_RANGE, LevelBasedValue.perLevel(1.5F, 1.0F), AttributeModifier.Operation.ADD_VALUE, UUID.fromString("164c937c-f04c-4730-b8e9-d299a3a187fa")))
                    .build(EnchiridionEnchantments.REACH.location());

            entries.add(EnchiridionEnchantments.ASHES_CURSE, ashesCurse);
            entries.add(EnchiridionEnchantments.REACH, reach);
        }

        @Override
        public String getName() {
            return Enchiridion.MOD_NAME + " Enchantments";
        }
    }

    public static class EnchantmentTagProvider extends FabricTagProvider<Enchantment> {
        public EnchantmentTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, Registries.ENCHANTMENT, registriesFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider wrapperLookup) {
            getOrCreateTagBuilder(EnchantmentTags.CURSE)
                    .add(EnchiridionEnchantments.ASHES_CURSE);

            getOrCreateTagBuilder(EnchantmentTags.NON_TREASURE)
                    .add(EnchiridionEnchantments.REACH);
            getOrCreateTagBuilder(EnchantmentTags.TREASURE)
                    .add(EnchiridionEnchantments.ASHES_CURSE);
        }
    }

    public static class ItemTagProvider extends FabricTagProvider.ItemTagProvider {
        public ItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(output, completableFuture, null);
        }

        @Override
        protected void addTags(HolderLookup.Provider wrapperLookup) {
            getOrCreateTagBuilder(Enchiridion.ItemTags.AXE_ENCHANTABLE)
                    .forceAddTag(ItemTags.AXES);
        }
    }
}
