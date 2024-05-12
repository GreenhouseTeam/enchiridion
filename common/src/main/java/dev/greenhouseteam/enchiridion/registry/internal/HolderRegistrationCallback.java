package dev.greenhouseteam.enchiridion.registry.internal;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface HolderRegistrationCallback<T> {
    Holder<T> register(Registry<T> registry, ResourceLocation id, T block);
}