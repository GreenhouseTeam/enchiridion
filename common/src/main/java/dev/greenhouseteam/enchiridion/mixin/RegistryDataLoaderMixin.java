package dev.greenhouseteam.enchiridion.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.Decoder;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.util.EnchiridionModifications;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
    @Shadow @Final
    private static RegistrationInfo NETWORK_REGISTRATION_INFO;

    @ModifyVariable(method = "loadElementFromResource", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/mojang/serialization/DataResult;getOrThrow()Ljava/lang/Object;"))
    private static <E> E enchiridion$loadModificationsFromResource(E original, WritableRegistry<E> registry, Decoder<E> decoder, RegistryOps<JsonElement> ops, ResourceKey<E> key, Resource resource, RegistrationInfo info) {
        if (info != NETWORK_REGISTRATION_INFO && key.isFor(Registries.ENCHANTMENT) && (resource.source().packId().equals("vanilla")) || Enchiridion.getHelper().isLoaderResourcePack(resource))
            return (E) EnchiridionModifications.modifyEnchantments((ResourceKey<Enchantment>) key, (Enchantment) original);
        return original;
    }
}
