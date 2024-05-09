package dev.greenhouseteam.enchiridion.util;

import com.mojang.datafixers.util.Pair;
import dev.greenhouseteam.enchiridion.enchantment.category.ItemEnchantmentCategories;
import dev.greenhouseteam.enchiridion.registry.EnchiridionDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class CreativeTabUtil {
    public static void sortEnchantmentsBasedOnCategory(List<ItemStack> stacks, HolderLookup.Provider provider) {
        if (stacks.stream().anyMatch(stack -> {
            ItemEnchantments enchantments = stack.getEnchantments();
            if (enchantments.isEmpty())
                enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            return !enchantments.isEmpty();
        })) {
            List<Pair<Integer, ItemStack>> indexList = new ArrayList<>(IntStream.range(0, stacks.size()).filter(i -> {
                ItemStack stack = stacks.get(i);
                ItemEnchantments enchantments = stack.getEnchantments();
                if (enchantments.isEmpty())
                    enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
                return !enchantments.isEmpty();
            }).mapToObj(i -> Pair.of(i, stacks.get(i))).toList());

            for (Pair<Integer, ItemStack> stack : indexList)
                stacks.set(stack.getFirst(), ItemStack.EMPTY);


            List<Integer> intList = indexList.stream().map(Pair::getFirst).toList();
            List<Item> itemList = indexList.stream().map(pair -> pair.getSecond().getItem()).toList();
            List<ItemStack> stackList = new ArrayList<>(indexList.stream().map(Pair::getSecond).toList());

            stackList.sort((o1, o2) -> {
                if (o1.getItem() != o2.getItem())
                    return Integer.compare(itemList.indexOf(o1.getItem()), itemList.indexOf(o2.getItem()));

                ItemEnchantments enchantments = o1.getEnchantments();
                if (enchantments.isEmpty())
                    enchantments = o1.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
                ItemEnchantments enchantments2 = o2.getEnchantments();
                if (enchantments2.isEmpty())
                    enchantments2 = o2.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

                Pair<Holder<Enchantment>, Integer> enchantment = EnchiridionUtil.getFirstEnchantmentAndLevel(provider, enchantments);
                Pair<Holder<Enchantment>, Integer> enchantment2 = EnchiridionUtil.getFirstEnchantmentAndLevel(provider, enchantments2);


                int o1CategoryPriority = Optional.ofNullable(EnchiridionUtil.getFirstEnchantmentCategory(provider, enchantments, o1.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY))).map(category -> {
                    if (!category.isBound())
                        return Integer.MIN_VALUE;
                    return category.value().priority();
                }).orElse(0);
                int o2CategoryPriority = Optional.ofNullable(EnchiridionUtil.getFirstEnchantmentCategory(provider, enchantments2, o2.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY))).map(category -> {
                    if (!category.isBound())
                        return Integer.MIN_VALUE;
                    return category.value().priority();
                }).orElse(0);

                if (enchantment != null && enchantment2 != null && enchantment.getFirst().equals(enchantment2.getFirst()))
                    return Integer.compare(enchantment.getSecond(), enchantment2.getSecond());

                if (o1CategoryPriority == o2CategoryPriority && enchantment != null && enchantment2 != null)
                    return compareEnchantments(enchantment.getFirst(), enchantment2.getFirst());

                // We flip the typical comparison, so we can get higher priority to come first.
                return Integer.compare(o2CategoryPriority, o1CategoryPriority);
            });

            for (int i = 0; i < intList.size(); ++i)
                stacks.set(intList.get(i), stackList.get(i));
        }
    }

    private static int compareEnchantments(Holder<Enchantment> enchantment, Holder<Enchantment> enchantment2) {
        if (
            // Prioritise the Minecraft namespace.
                enchantment.unwrapKey().isPresent() && enchantment.unwrapKey().get().location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) &&
                        enchantment2.unwrapKey().isPresent() && !enchantment2.unwrapKey().get().location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)
        ) {
            return -1;
        } else if (
            // Prioritise the Minecraft namespace.
                enchantment.unwrapKey().isPresent() && !enchantment.unwrapKey().get().location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) &&
                        enchantment2.unwrapKey().isPresent() && enchantment2.unwrapKey().get().location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)
        ) {
            return 1;
        }
        if (enchantment.unwrapKey().isPresent() && enchantment2.unwrapKey().isPresent())
            return enchantment.unwrapKey().get().location().compareTo(enchantment2.unwrapKey().get().location());
        return Integer.compare(enchantment.hashCode(), enchantment2.hashCode());
    }
}
