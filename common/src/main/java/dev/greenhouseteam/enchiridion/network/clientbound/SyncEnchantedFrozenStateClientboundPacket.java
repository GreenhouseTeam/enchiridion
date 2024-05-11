package dev.greenhouseteam.enchiridion.network.clientbound;

import dev.greenhouseteam.enchiridion.Enchiridion;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record SyncEnchantedFrozenStateClientboundPacket(int entityId, boolean value) implements CustomPacketPayload {
    public static final ResourceLocation ID = Enchiridion.asResource("sync_enchanted_frozen_state");
    public static final Type<SyncEnchantedFrozenStateClientboundPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SyncEnchantedFrozenStateClientboundPacket> STREAM_CODEC = CustomPacketPayload.codec(
            SyncEnchantedFrozenStateClientboundPacket::write, SyncEnchantedFrozenStateClientboundPacket::new
    );

    public SyncEnchantedFrozenStateClientboundPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readBoolean());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(value);
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            Enchiridion.getHelper().setFrozenByEnchantment(entity, value);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
