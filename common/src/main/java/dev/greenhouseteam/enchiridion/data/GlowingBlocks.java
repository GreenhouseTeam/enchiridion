package dev.greenhouseteam.enchiridion.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.List;

public record GlowingBlocks(List<BlockPos> glowingBlocks, int highlightColor) {
    public static final Codec<GlowingBlocks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("glowingBlocks").forGetter(GlowingBlocks::glowingBlocks),
            Codec.INT.fieldOf("highlightColor").forGetter(GlowingBlocks::highlightColor)
    ).apply(instance, GlowingBlocks::new));
}
