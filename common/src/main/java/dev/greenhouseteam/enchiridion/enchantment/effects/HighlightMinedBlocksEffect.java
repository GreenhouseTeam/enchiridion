package dev.greenhouseteam.enchiridion.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.network.clientbound.SetOutlinedBlocksClientboundPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public record HighlightMinedBlocksEffect(LevelBasedValue duration, LevelBasedValue range, TagKey<Block> blacklist, boolean invertList) implements BlockMinedEffect, EnchantmentLocationBasedEffect {
    public static final MapCodec<HighlightMinedBlocksEffect> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            LevelBasedValue.CODEC.fieldOf("duration").forGetter(HighlightMinedBlocksEffect::duration),
            LevelBasedValue.CODEC.fieldOf("range").forGetter(HighlightMinedBlocksEffect::range),
            TagKey.codec(Registries.BLOCK).fieldOf("blacklist").forGetter(HighlightMinedBlocksEffect::blacklist),
            Codec.BOOL.fieldOf("invert_list").forGetter(HighlightMinedBlocksEffect::invertList)
    ).apply(inst, HighlightMinedBlocksEffect::new));


    @Override
    public void onChangedBlock(ServerLevel var1, int var2, EnchantedItemInUse var3, Entity var4, Vec3 var5, boolean var6) {
        // No-op
    }

    @Override
    public MapCodec<? extends EnchantmentLocationBasedEffect> codec() {
        return CODEC;
    }


    @Override
    public void onBlockMined(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, BlockState block) {
        LivingEntity livingEntity = enchantedItemInUse.owner();

        if (block.is(blacklist) != invertList) return;
        if (enchantedItemInUse.itemStack().getItem() instanceof DiggerItem diggerItem && !diggerItem.isCorrectToolForDrops(enchantedItemInUse.itemStack(), block)) return;
        if (livingEntity instanceof ServerPlayer player) {
            int range = (int) this.range.calculate(i);
            int duration = (int) this.duration.calculate(i);
            int color = block.getBlock().defaultMapColor().calculateRGBColor(MapColor.Brightness.HIGH);
            // Highlight the mined block
            Iterator<BlockPos> blocks = BlockPos.withinManhattan(
                    player.blockPosition(),
                    range,
                    range,
                    range
            ).iterator();
            List<BlockPos> blocksList = new ArrayList<>();
            while (blocks.hasNext()) {
                BlockPos pos = blocks.next();
                BlockState state = player.level().getBlockState(pos);
                if (state.getBlock().equals(block.getBlock())) {
                    blocksList.add(new BlockPos(pos)); // To deal with mutable BlockPos
                }
            }

            Enchiridion.getHelper().sendClientbound(player, new SetOutlinedBlocksClientboundPacket(
                    blocksList,
                    color,
                    duration

            ));

        }

    }

}
