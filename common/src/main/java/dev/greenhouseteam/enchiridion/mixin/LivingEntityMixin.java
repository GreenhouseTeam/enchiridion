package dev.greenhouseteam.enchiridion.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.datafixers.util.Pair;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.access.EntityPostEntityDropParamsAccess;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.TargetedConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements EntityPostEntityDropParamsAccess {
    @Shadow public abstract boolean canFreeze();

    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot var1);

    @Shadow public abstract boolean isAlive();

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

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;canFreeze()Z", ordinal = 0))
    private boolean enchiridion$preventPowderSnowLogicIfEnchantmentFrozen(boolean original) {
        return original && !Enchiridion.getHelper().isFrozenByEnchantment(this);
    }

    @WrapWithCondition(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setTicksFrozen(I)V", ordinal = 1))
    private boolean enchiridion$preventUnfreezingIfEnchantmentFrozenAndInPowderSnow(LivingEntity instance, int i) {
         return !(isInPowderSnow && Enchiridion.getHelper().isFrozenByEnchantment(instance));
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;blockUsingShield(Lnet/minecraft/world/entity/LivingEntity;)V", shift = At.Shift.AFTER))
    private void enchiridion$postShieldDisableActions(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!(source.getDirectEntity() instanceof LivingEntity attacker) || !attacker.canDisableShield() || this.level().isClientSide())
            return;
        LootParams.Builder params = new LootParams.Builder((ServerLevel)this.level());
        params.withParameter(LootContextParams.THIS_ENTITY, this);
        params.withParameter(LootContextParams.DAMAGE_SOURCE, source);
        params.withParameter(LootContextParams.ORIGIN, this.position());
        params.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity());
        params.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity());

        for (Pair<List<TargetedConditionalEffect<EnchantmentEntityEffect>>, Integer> entry : attacker.getMainHandItem().getEnchantments().entrySet().stream().filter(entry -> entry.getKey().isBound() && entry.getKey().value().matchingSlot(EquipmentSlot.MAINHAND) && !entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.POST_SHIELD_DISABLE).isEmpty()).map(entry -> Pair.of(entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.POST_SHIELD_DISABLE), entry.getIntValue())).toList()) {
            params.withParameter(LootContextParams.ENCHANTMENT_LEVEL, entry.getSecond());
            LootContext context = new LootContext.Builder(params.create(LootContextParamSets.ENCHANTED_DAMAGE)).create(Optional.empty());
            for (TargetedConditionalEffect<EnchantmentEntityEffect> effect : entry.getFirst()) {
                if (effect.matches(context)) {
                    Entity entity = switch (effect.affected()) {
                        case ATTACKER -> source.getEntity();
                        case DAMAGING_ENTITY -> source.getDirectEntity();
                        case VICTIM -> this;
                    };
                    if (entity != null)
                        effect.effect().apply((ServerLevel)this.level(), entry.getSecond(), new EnchantedItemInUse(attacker.getMainHandItem(), EquipmentSlot.MAINHAND, attacker), entity, entity.position());
                }
            }
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            for (Pair<List<TargetedConditionalEffect<EnchantmentEntityEffect>>, Integer> entry : this.getItemBySlot(slot).getEnchantments().entrySet().stream().filter(entry -> entry.getKey().isBound() && entry.getKey().value().matchingSlot(slot) && entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.POST_SHIELD_DISABLE).isEmpty()).map(entry -> Pair.of(entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.POST_SHIELD_DISABLE), entry.getIntValue())).toList()) {
                params.withParameter(LootContextParams.ENCHANTMENT_LEVEL, entry.getSecond());
                LootContext context = new LootContext.Builder(params.create(LootContextParamSets.ENCHANTED_DAMAGE)).create(Optional.empty());
                for (TargetedConditionalEffect<EnchantmentEntityEffect> effect : entry.getFirst()) {
                    if (effect.matches(context)) {
                        Entity entity = switch (effect.affected()) {
                            case ATTACKER -> source.getEntity();
                            case DAMAGING_ENTITY -> source.getDirectEntity();
                            case VICTIM -> this;
                        };
                        if (entity != null)
                            effect.effect().apply((ServerLevel)this.level(), entry.getSecond(), new EnchantedItemInUse(attacker.getItemBySlot(slot), slot, (LivingEntity)(Object)this), entity, entity.position());
                    }
                }
            }
        }
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeFrost()V"))
    private void enchiridion$removeEnchantmentFrost(CallbackInfo ci) {
        if (Enchiridion.getHelper().isFrozenByEnchantment(this) && getTicksFrozen() <= 0)
            Enchiridion.getHelper().setFrozenByEnchantment(this, false);
    }

    @ModifyExpressionValue(method = "tryAddFrost", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean enchiridion$allowSpeedDebuffWhenEnchantedFrost(boolean original) {
        return original && !Enchiridion.getHelper().isFrozenByEnchantment(this);
    }

    @ModifyVariable(method = "canFreeze", at = @At(value = "STORE"), index = 1)
    private boolean enchiridion$allowFreezingArmorWearersWithEnchantment(boolean original) {
        return original || Enchiridion.getHelper().isFrozenByEnchantment(this);
    }
}
