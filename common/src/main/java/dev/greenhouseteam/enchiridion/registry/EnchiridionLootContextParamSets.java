package dev.greenhouseteam.enchiridion.registry;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class EnchiridionLootContextParamSets {
    public static final LootContextParamSet ENCHANTED_DROPS = LootContextParamSet.builder()
            .required(LootContextParams.THIS_ENTITY)
            .required(EnchiridionLootContextParams.ITEM_ENTITY)
            .build();
}
