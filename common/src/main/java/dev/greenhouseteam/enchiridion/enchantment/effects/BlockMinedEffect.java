package dev.greenhouseteam.enchiridion.enchantment.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;


/**
 * Represents an enchantment effect that is triggered when a block is mined by a player.
 */
public interface BlockMinedEffect {

    void onBlockMined(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, BlockState block);
}
