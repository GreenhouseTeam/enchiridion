package dev.greenhouseteam.enchiridion.registry;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.enchiridion.Enchiridion;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class EnchiridionAttachments {
    public static final AttachmentType<Boolean> FROZEN_BY_ENCHANTMENT = AttachmentRegistry
            .createPersistent(Enchiridion.asResource("frozen_by_enchantment"), Codec.BOOL);
}
