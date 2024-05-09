package dev.greenhouseteam.enchiridion.registry;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.enchantment.category.ItemEnchantmentCategories;
import dev.greenhouseteam.enchiridion.registry.internal.RegistrationCallback;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public class EnchiridionDataComponents {
    public static final DataComponentType<ItemEnchantmentCategories> ENCHANTMENT_CATEGORIES = DataComponentType.<ItemEnchantmentCategories>builder()
            .persistent(ItemEnchantmentCategories.CODEC)
            .networkSynchronized(ItemEnchantmentCategories.STREAM_CODEC)
            .cacheEncoding()
            .build();

    public static void registerAll(RegistrationCallback<DataComponentType<?>> callback) {
        callback.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Enchiridion.asResource("enchantment_categories"), ENCHANTMENT_CATEGORIES);
    }
}
