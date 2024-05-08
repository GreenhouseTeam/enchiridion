package dev.greenhouseteam.enchiridion.registry;

import dev.greenhouseteam.enchiridion.Enchiridion;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class EnchiridionLootContextParams {
    public static final LootContextParam<ItemEntity> ITEM_ENTITY = new LootContextParam<>(Enchiridion.asResource("item_entity"));
}
