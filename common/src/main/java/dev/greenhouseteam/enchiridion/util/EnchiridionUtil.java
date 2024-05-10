package dev.greenhouseteam.enchiridion.util;

import com.mojang.datafixers.util.Pair;
import dev.greenhouseteam.enchiridion.enchantment.category.EnchantmentCategory;
import dev.greenhouseteam.enchiridion.enchantment.category.ItemEnchantmentCategories;
import dev.greenhouseteam.enchiridion.registry.EnchiridionDataComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class EnchiridionUtil {

    @Nullable
    public static Pair<Holder<Enchantment>, Integer> getFirstEnchantmentAndLevel(HolderLookup.Provider registries, ItemEnchantments enchantments) {
        Optional<HolderSet.Named<Enchantment>> tooltipOrderTag = registries.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.TOOLTIP_ORDER);
        if (tooltipOrderTag.isEmpty())
            return null;

        Optional<Pair<Holder<Enchantment>, Integer>> enchantment = tooltipOrderTag.get().stream().filter(holder -> enchantments.keySet().contains(holder)).map(holder -> Pair.of(holder, enchantments.getLevel(holder))).findFirst();
        if (enchantment.isEmpty())
            enchantment = enchantments.keySet().stream().findFirst().map(holder -> Pair.of(holder, enchantments.getLevel(holder)));
        return enchantment.orElse(null);
    }

    @Nullable
    public static Holder<EnchantmentCategory> getFirstEnchantmentCategory(HolderLookup.Provider registries, ItemEnchantments enchantments, ItemEnchantmentCategories categories) {
        Optional<HolderSet.Named<Enchantment>> tooltipOrderTag = registries.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.TOOLTIP_ORDER);
        if (tooltipOrderTag.isEmpty())
            return null;

        List<Holder<Enchantment>> enchantment = tooltipOrderTag.get().stream().filter(e -> enchantments.keySet().contains(e)).toList();

        if (enchantment.isEmpty())
            return enchantments.keySet().stream().map(categories::findFirstCategory).filter(holder -> holder != null && holder.isBound()).max(Comparator.comparingInt(value -> value.value().priority())).orElse(null);

        return enchantment.stream().map(categories::findFirstCategory).filter(holder -> holder != null && holder.isBound()).max(Comparator.comparingInt(value -> value.value().priority())).orElse(null);
    }

    @Nullable
    public static Holder<EnchantmentCategory> getFirstEnchantmentCategory(HolderLookup.Provider registries, Holder<Enchantment> enchantment) {
        return registries.lookupOrThrow(EnchiridionRegistries.ENCHANTMENT_CATEGORY).listElements().filter(category -> category.isBound() && category.value().acceptedEnchantments().contains(enchantment)).max(Comparator.comparingInt(value -> value.value().priority())).orElse(null);
    }

    public static boolean isValidInCategory(HolderLookup.Provider registries, ItemEnchantmentCategories categories, Holder<Enchantment> enchantment) {
        Optional<Holder.Reference<EnchantmentCategory>> category = registries.lookupOrThrow(EnchiridionRegistries.ENCHANTMENT_CATEGORY).listElements().filter(c -> c.isBound() && c.value().acceptedEnchantments().contains(enchantment)).max((o1, o2) -> {
            if (!categoryAcceptsNewEnchantments(o1, categories, enchantment))
                return -1;
            if (!categoryAcceptsNewEnchantments(o2, categories, enchantment))
                return 1;
            return Integer.compare(o1.value().priority(), o2.value().priority());
        });
        return category.map(enchantmentCategory -> categoryAcceptsNewEnchantments(enchantmentCategory, categories, enchantment)).orElse(true);
    }

    public static boolean categoryAcceptsNewEnchantments(Holder<EnchantmentCategory> category, ItemEnchantmentCategories categories, Holder<Enchantment> enchantment) {
        if (category == null || !category.isBound() || category.value().allowed().isEmpty())
            return true;
        return categoryAcceptsNewEnchantmentsInternal(category, categories.getCategories().get(category), enchantment);
    }

    public static boolean categoryAcceptsNewEnchantmentsWithValue(Holder<EnchantmentCategory> category, ItemEnchantmentCategories categories, Holder<Enchantment> enchantment) {
        if (category == null || !category.isBound() || category.value().allowed().isEmpty())
            return true;
        List<Holder<Enchantment>> enchantments = new ArrayList<>(categories.getCategories().get(category));
        if (!enchantments.contains(enchantment))
            enchantments.add(enchantment);
        return categoryAcceptsNewEnchantmentsInternal(category, enchantments, enchantment);
    }

    private static boolean categoryAcceptsNewEnchantmentsInternal(Holder<EnchantmentCategory> category, List<Holder<Enchantment>> holders, Holder<Enchantment> enchantment) {
        return (category.isBound() && category.value().allowed().isEmpty()) || category.isBound() && holders.subList(0, holders.indexOf(enchantment) + 1).size() < category.value().allowed().get() + 1;
    }

    public static void modifyEnchantmentTooltips(ItemStack stack, Item.TooltipContext tooltipContext, TooltipFlag flag, List<Component> components) {
        if (tooltipContext.registries() == null)
            return;

        ItemEnchantmentCategories categories = stack.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY);

        List<Component> enchantmentComponents = new ArrayList<>();
        getEnchantmentsOrStoredEnchantments(stack).addToTooltip(tooltipContext, enchantmentComponents::add, flag);

        components.sort((o1, o2) -> {
            if (!enchantmentComponents.contains(o1) || !enchantmentComponents.contains(o2))
                return Integer.compare(components.indexOf(o1), components.indexOf(o2));

            Optional<Holder.Reference<Enchantment>> o1Enchantment = tooltipContext.registries().lookupOrThrow(Registries.ENCHANTMENT).filterElements(e -> o1.contains(e.description())).listElements().findFirst();
            Optional<Holder.Reference<Enchantment>> o2Enchantment = tooltipContext.registries().lookupOrThrow(Registries.ENCHANTMENT).filterElements(e -> o2.contains(e.description())).listElements().findFirst();

            if (o1Enchantment.isEmpty() || o2Enchantment.isEmpty())
                return Integer.compare(components.indexOf(o1), components.indexOf(o2));

            Holder<EnchantmentCategory> category = categories.findFirstCategory(o1Enchantment.get());
            Holder<EnchantmentCategory> category2 = categories.findFirstCategory(o2Enchantment.get());

            if (category == null || !category.isBound() || category2 == null || !category2.isBound())
                return Integer.compare(components.indexOf(o1), components.indexOf(o2));

            return Integer.compare(category2.value().priority(), category.value().priority());
        });

        for (int i = 0; i < components.size(); ++i) {
            Component component = components.get(i);
            if (enchantmentComponents.contains(component)) {
                Optional<Holder.Reference<Enchantment>> enchantment = tooltipContext.registries().lookupOrThrow(Registries.ENCHANTMENT).filterElements(e -> component.contains(e.description())).listElements().findFirst();
                if (enchantment.isEmpty())
                    return;
                Holder<EnchantmentCategory> category = categories.findFirstCategory(enchantment.get());
                if (category == null || !category.isBound())
                    return;
                components.set(i, components.get(i).copy().withColor(category.value().color().getValue()));
            }
        }
    }

    public static ItemEnchantments getEnchantmentsOrStoredEnchantments(ItemStack stack) {
        ItemEnchantments enchantments = stack.getEnchantments();
        if (enchantments.isEmpty())
            enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        return enchantments;
    }
}
