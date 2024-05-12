package dev.greenhouseteam.enchiridion.mixin.fabric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.registry.EnchiridionAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
    @ModifyExpressionValue(method = "getDestroyProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F"))
    private float enchiridion$conditionedMiningSpeed(float original, BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos) {
        if (BuiltInRegistries.BLOCK.getTag(Enchiridion.BlockTags.BASE_STONE).orElseThrow().contains(blockState.getBlock().builtInRegistryHolder())) {
            AttributeInstance attributeInstance = player.getAttribute(EnchiridionAttributes.BASE_STONE_MINING_SPEED);
            if (attributeInstance != null)
                return original *= (float)attributeInstance.getValue();
        }
        return original;
    }
}
