package dev.greenhouseteam.enchiridion.mixin;

import dev.greenhouseteam.enchiridion.access.EntityPostEntityDropParamsAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements EntityPostEntityDropParamsAccess {
    @Unique
    private static LootParams.Builder enchiridion$builder;

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "dropFromLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V"))
    private void enchiridion$captureEntityDropBuilder(DamageSource source, boolean hasLastHurtByPlayer, CallbackInfo ci) {
        if (!(level() instanceof ServerLevel serverLevel))
            return;
        enchiridion$builder = new LootParams.Builder(serverLevel);
        enchiridion$builder.withParameter(LootContextParams.THIS_ENTITY, this);
        enchiridion$builder.withParameter(LootContextParams.DAMAGE_SOURCE, source);
        enchiridion$builder.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity());
        enchiridion$builder.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity());
    }

    @Inject(method = "dropFromLootTable", at = @At("TAIL"))
    private void enchiridion$resetEntityBuilder(DamageSource source, boolean hasLastHurtByPlayer, CallbackInfo ci) {
        enchiridion$builder = null;
    }

    @Override
    public LootParams.Builder enchiridion$getPostEntityDropsParams() {
        return enchiridion$builder;
    }
}
