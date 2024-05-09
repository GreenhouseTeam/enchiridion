package dev.greenhouseteam.enchiridion.enchantment.category;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import dev.greenhouseteam.enchiridion.registry.EnchiridionRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemEnchantmentCategories {
    public static final ItemEnchantmentCategories EMPTY = new ItemEnchantmentCategories(ImmutableMap.of());
    public static final Codec<ItemEnchantmentCategories> CODEC = new UnboundedMapCodec<>(EnchantmentCategory.CODEC, Enchantment.CODEC.listOf()).xmap(ItemEnchantmentCategories::new, ItemEnchantmentCategories::getCategories);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemEnchantmentCategories> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.map(HashMap::new, ByteBufCodecs.holderRegistry(EnchiridionRegistries.ENCHANTMENT_CATEGORY), ByteBufCodecs.<RegistryFriendlyByteBuf, Holder<Enchantment>>list().apply(ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT))), ItemEnchantmentCategories::getCategories, ItemEnchantmentCategories::new);

    private final Map<Holder<EnchantmentCategory>, List<Holder<Enchantment>>> enchantmentCategories;

    public ItemEnchantmentCategories() {
        this.enchantmentCategories = new HashMap<>();
    }

    public ItemEnchantmentCategories(Map<Holder<EnchantmentCategory>, List<Holder<Enchantment>>> enchantmentCategories) {
        this.enchantmentCategories = enchantmentCategories;
    }

    public Map<Holder<EnchantmentCategory>, List<Holder<Enchantment>>> getCategories() {
        return Map.copyOf(enchantmentCategories);
    }

    @Nullable
    public Holder<EnchantmentCategory> findFirstCategory(Holder<Enchantment> enchantment) {
        return enchantmentCategories.entrySet().stream().filter(entry -> entry.getValue().contains(enchantment)).max(Comparator.comparingInt(value -> !value.getKey().isBound() ? -1 : value.getKey().value().priority())).map(Map.Entry::getKey).orElse(null);
    }

    public void addCategoryWithEnchantment(Holder<EnchantmentCategory> category, Holder<Enchantment> enchantment) {
        enchantmentCategories.computeIfAbsent(category, category1 -> new ArrayList<>()).add(enchantment);
    }

    public void removeCategoryWithEnchantment(Holder<EnchantmentCategory> category, Holder<Enchantment> enchantment) {
        enchantmentCategories.computeIfAbsent(category, category1 -> new ArrayList<>()).add(enchantment);
    }
}
