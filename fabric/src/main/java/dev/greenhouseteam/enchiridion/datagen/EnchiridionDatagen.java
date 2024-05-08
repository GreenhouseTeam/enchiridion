package dev.greenhouseteam.enchiridion.datagen;

import com.mojang.serialization.Lifecycle;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantments;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
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

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.ENCHANTMENT, EnchiridionEnchantments::bootstrap);
    }

    public static class EnchantmentProvider extends FabricDynamicRegistryProvider {
        public EnchantmentProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void configure(HolderLookup.Provider registries, Entries entries) {
            EnchiridionEnchantments.bootstrap(new BootstrapContext<>() {
                @Override
                public Holder.Reference<Enchantment> register(ResourceKey<Enchantment> resourceKey, Enchantment object, Lifecycle lifecycle) {
                    return (Holder.Reference<Enchantment>) entries.add(resourceKey, object);
                }

                @Override
                public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> resourceKey) {
                    return registries.lookupOrThrow(resourceKey);
                }
            });
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
            getOrCreateTagBuilder(Enchiridion.ItemTags.ASHES_ENCHANTABLE)
                    .forceAddTag(ItemTags.WEAPON_ENCHANTABLE)
                    .forceAddTag(ItemTags.MINING_ENCHANTABLE);
        }
    }
}
