package dev.greenhouseteam.enchiridion.network.clientbound;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.access.LevelUpEnchantmentMenuAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.EnchantmentMenu;

public record SyncEnchantScrollIndexClientboundPacket(int value) implements CustomPacketPayload {
    public static final ResourceLocation ID = Enchiridion.asResource("sync_enchant_scroll_index_clientbound");
    public static final Type<SyncEnchantScrollIndexClientboundPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SyncEnchantScrollIndexClientboundPacket> STREAM_CODEC = CustomPacketPayload.codec(
            SyncEnchantScrollIndexClientboundPacket::write, SyncEnchantScrollIndexClientboundPacket::new
    );

    public SyncEnchantScrollIndexClientboundPacket(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(value);
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player.containerMenu instanceof EnchantmentMenu enchantmentMenu) {
                ((LevelUpEnchantmentMenuAccess)enchantmentMenu).enchiridion$setScrollOffset(value);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
