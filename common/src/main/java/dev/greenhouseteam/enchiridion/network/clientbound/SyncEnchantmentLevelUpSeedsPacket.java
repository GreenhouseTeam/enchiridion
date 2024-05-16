package dev.greenhouseteam.enchiridion.network.clientbound;

import dev.greenhouseteam.enchiridion.Enchiridion;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record SyncEnchantmentLevelUpSeedsPacket(int entityId, int index, int seed, boolean clear) implements CustomPacketPayload {
    public static final ResourceLocation ID = Enchiridion.asResource("sync_enchantment_level_up_seeds");
    public static final Type<SyncEnchantmentLevelUpSeedsPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SyncEnchantmentLevelUpSeedsPacket> STREAM_CODEC = CustomPacketPayload.codec(
            SyncEnchantmentLevelUpSeedsPacket::write, SyncEnchantmentLevelUpSeedsPacket::new
    );

    public static SyncEnchantmentLevelUpSeedsPacket add(int entityId, int index, int seed) {
        return new SyncEnchantmentLevelUpSeedsPacket(entityId, index, seed, false);
    }

    public static SyncEnchantmentLevelUpSeedsPacket clear(int entityId) {
        return new SyncEnchantmentLevelUpSeedsPacket(entityId, -1, -1, true);
    }

    public SyncEnchantmentLevelUpSeedsPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(index);
        buf.writeInt(seed);
        buf.writeBoolean(clear);
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            if (!(entity instanceof Player player))
                return;
            if (clear)
                Enchiridion.getHelper().clearEnchantmentSeeds(player);
            else
                Enchiridion.getHelper().addEnchantmentSeed(player, index, seed);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
