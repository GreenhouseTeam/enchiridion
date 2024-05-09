package dev.greenhouseteam.enchiridion.datagen;

import com.mojang.serialization.Lifecycle;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentCategories;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantments;
import dev.greenhouseteam.enchiridion.registry.EnchiridionRegistries;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.concurrent.CompletableFuture;

public class EnchiridionDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(EnchantmentTagProvider::new);
        pack.addProvider(DynamicRegistryProvider::new);
        pack.addProvider(ItemTagProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.ENCHANTMENT, EnchiridionEnchantments::bootstrap);
        registryBuilder.add(EnchiridionRegistries.ENCHANTMENT_CATEGORY, EnchiridionEnchantmentCategories::bootstrap);
    }

    public static class DynamicRegistryProvider extends FabricDynamicRegistryProvider {
        public DynamicRegistryProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void configure(HolderLookup.Provider registries, Entries entries) {
            EnchiridionEnchantments.bootstrap(createContext(registries, entries));
            EnchiridionEnchantmentCategories.bootstrap(createContext(registries, entries));
        }

        private static <T> BootstrapContext<T> createContext(HolderLookup.Provider registries, Entries entries) {
            return new BootstrapContext<>() {
                @Override
                public Holder.Reference<T> register(ResourceKey<T> resourceKey, T object, Lifecycle lifecycle) {
                    return (Holder.Reference<T>) entries.add(resourceKey, object);
                }

                @Override
                public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> resourceKey) {
                    return registries.lookupOrThrow(resourceKey);
                }
            };
        }

        @Override
        public String getName() {
            return Enchiridion.MOD_NAME + " Dynamic Registries";
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

            getOrCreateTagBuilder(Enchiridion.EnchantmentTags.PRIMARY_CATEGORY)
                    .add(
                            Enchantments.DEPTH_STRIDER,
                            Enchantments.FROST_WALKER,
                            Enchantments.LOOTING,
                            Enchantments.LOYALTY,
                            Enchantments.LUCK_OF_THE_SEA,
                            Enchantments.FORTUNE,
                            Enchantments.MULTISHOT,
                            Enchantments.PIERCING,
                            Enchantments.RESPIRATION,
                            Enchantments.RIPTIDE,
                            Enchantments.SILK_TOUCH,
                            Enchantments.THORNS,
                            Enchantments.WIND_BURST
                    );
            getOrCreateTagBuilder(Enchiridion.EnchantmentTags.SECONDARY_CATEGORY)
                    .add(
                            Enchantments.AQUA_AFFINITY,
                            Enchantments.CHANNELING,
                            Enchantments.BREACH,
                            Enchantments.FIRE_ASPECT,
                            Enchantments.FLAME,
                            Enchantments.KNOCKBACK,
                            Enchantments.INFINITY,
                            Enchantments.PUNCH,
                            Enchantments.SOUL_SPEED,
                            Enchantments.SWEEPING_EDGE,
                            Enchantments.SWIFT_SNEAK,
                            EnchiridionEnchantments.REACH
                    );
            getOrCreateTagBuilder(Enchiridion.EnchantmentTags.TERTIARY_CATEGORY)
                    .add(
                            Enchantments.BANE_OF_ARTHROPODS,
                            Enchantments.BLAST_PROTECTION,
                            Enchantments.DENSITY,
                            Enchantments.EFFICIENCY,
                            Enchantments.FEATHER_FALLING,
                            Enchantments.FIRE_PROTECTION,
                            Enchantments.IMPALING,
                            Enchantments.LURE,
                            Enchantments.POWER,
                            Enchantments.PROJECTILE_PROTECTION,
                            Enchantments.PROTECTION,
                            Enchantments.QUICK_CHARGE,
                            Enchantments.SHARPNESS,
                            Enchantments.SMITE
                    );
            getOrCreateTagBuilder(Enchiridion.EnchantmentTags.UNCATEGORISED_CATEGORY)
                    .add(
                            Enchantments.MENDING,
                            Enchantments.UNBREAKING
                    );
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
