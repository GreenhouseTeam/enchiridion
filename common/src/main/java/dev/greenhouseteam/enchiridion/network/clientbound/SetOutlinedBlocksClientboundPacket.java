package dev.greenhouseteam.enchiridion.network.clientbound;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.client.GlowingBlocksClientHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SetOutlinedBlocksClientboundPacket(List<BlockPos> outlinedBlocks, int color, int duration) implements CustomPacketPayload {
    public static final ResourceLocation ID = Enchiridion.asResource("set_outlined_blocks");
    public static final Type<SetOutlinedBlocksClientboundPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SetOutlinedBlocksClientboundPacket> STREAM_CODEC = CustomPacketPayload.codec(
            SetOutlinedBlocksClientboundPacket::write, SetOutlinedBlocksClientboundPacket::new
    );

    public SetOutlinedBlocksClientboundPacket(FriendlyByteBuf buf) {
        this(buf.readCollection(ArrayList::new, BlockPos.STREAM_CODEC), buf.readInt(), buf.readInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(outlinedBlocks, BlockPos.STREAM_CODEC);
        buf.writeInt(color);
        buf.writeInt(duration);
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            GlowingBlocksClientHolder.setGlowingBlocks(outlinedBlocks, color, duration);
        });
    }
}
