package dev.greenhouseteam.enchiridion.network.serverbound;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.access.LevelUpEnchantmentMenuAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.EnchantmentMenu;

public record SyncEnchantScrollIndexServerboundPacket(int value) implements CustomPacketPayload {
    public static final ResourceLocation ID = Enchiridion.asResource("sync_enchant_scroll_index");
    public static final Type<SyncEnchantScrollIndexServerboundPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SyncEnchantScrollIndexServerboundPacket> STREAM_CODEC = CustomPacketPayload.codec(
            SyncEnchantScrollIndexServerboundPacket::write, SyncEnchantScrollIndexServerboundPacket::new
    );

    public SyncEnchantScrollIndexServerboundPacket(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(value);
    }

    public void handle(ServerPlayer player) {
        player.getServer().execute(() -> {
            if (!(player.containerMenu instanceof EnchantmentMenu enchantmentMenu))
                return;
            ((LevelUpEnchantmentMenuAccess)enchantmentMenu).enchiridion$setScrollOffset(value);
            ((LevelUpEnchantmentMenuAccess)enchantmentMenu).enchiridion$refreshEnchantmentIndexes();
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
