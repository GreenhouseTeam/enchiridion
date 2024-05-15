package dev.greenhouseteam.enchiridion.util;

import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EnchantingTableUtil {
    public static int getLevelUpCost(int level, int maxLevel, ItemStack stack) {
        Item item = stack.getItem();
        int enchantmentValue = item.getEnchantmentValue();
        if (enchantmentValue <= 0) {
            return 25;
        }
        return Math.clamp(Mth.floor((((float)level / (float)maxLevel) * 12.0F) + ((20.0F / enchantmentValue) * 3.0)), 1, 25);
    }

    public static int getMinimumBookshelfAmountForLevelling(int level, int maxLevel) {
        return Mth.floor(((float)level / (float)maxLevel) * 15.0F);
    }

    public static int getLapisCountForLevelling(int level, int maxLevel) {
        return Mth.floor(((float)level / (float)maxLevel) * 4.0F);
    }
}
