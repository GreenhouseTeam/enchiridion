package dev.greenhouseteam.enchiridion.registry;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.registry.internal.HolderRegistrationCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class EnchiridionAttributes {
    public static Holder<Attribute> BASE_STONE_MINING_SPEED;

    public static void registerAll(HolderRegistrationCallback<Attribute> callback) {
        BASE_STONE_MINING_SPEED = callback.register(BuiltInRegistries.ATTRIBUTE, Enchiridion.asResource("player.base_stone_mining_speed"), new RangedAttribute("attribute.name.player.enchiridion.base_stone_mining_speed", 1.0, 0.0, 1024.0).setSyncable(true));
    }
}
