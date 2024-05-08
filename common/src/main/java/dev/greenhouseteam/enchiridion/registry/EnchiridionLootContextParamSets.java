package dev.greenhouseteam.enchiridion.registry;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class EnchiridionLootContextParamSets {
    public static final LootContextParamSet BLOCK_DROP = LootContextParamSet.builder()
            .required(LootContextParams.TOOL)
            .required(EnchiridionLootContextParams.ITEM_ENTITY)
            .required(LootContextParams.BLOCK_STATE)
            .optional(LootContextParams.THIS_ENTITY)
            .optional(LootContextParams.BLOCK_ENTITY)
            .build();
}
