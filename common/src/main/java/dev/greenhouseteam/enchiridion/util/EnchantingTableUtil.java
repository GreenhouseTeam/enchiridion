package dev.greenhouseteam.enchiridion.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class EnchantingTableUtil {

    public static int getLevelUpCost(RandomSource random, int level, int maxLevel) {
        return Math.clamp(Mth.floor(((float)level / (float)maxLevel) * 18.0F) + Mth.floor((random.nextFloat() * 3.0F) - 1.0F), 1, 25);
    }

    public static int getMinimumBookshelfAmountForLevelling(int level, int maxLevel) {
        return Mth.floor(((float)level / (float)maxLevel) * 15.0F);
    }

    public static int getLapisCountForLevelling(int level, int maxLevel) {
        return Mth.floor(((float)level / (float)maxLevel) * 4.0F);
    }

    public static int potentiallyAddExtraLevel(RandomSource random, int level, int maxLevel, int enchantmentValue) {
        float chance = (float)enchantmentValue / 120.0F;
        return Math.min(level + (random.nextFloat() < chance ? 1 : 0), maxLevel);
    }
}
