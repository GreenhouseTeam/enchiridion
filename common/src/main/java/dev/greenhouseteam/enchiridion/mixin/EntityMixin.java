package dev.greenhouseteam.enchiridion.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.access.EntityPostEntityDropParamsAccess;
import dev.greenhouseteam.enchiridion.access.EntityRidingEffectsAccess;
import dev.greenhouseteam.enchiridion.enchantment.effects.RidingConditionalEffect;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionLootContextParamSets;
import dev.greenhouseteam.enchiridion.registry.EnchiridionLootContextParams;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityRidingEffectsAccess {
    @Shadow public abstract Level level();

    @Shadow public abstract boolean canFreeze();

    @ModifyReturnValue(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "RETURN", ordinal = 2))
    private ItemEntity enchiridion$postEntityDropEffectComponents(ItemEntity original) {
        if (!(this.level() instanceof ServerLevel serverLevel) || !((Entity)(Object)this instanceof EntityPostEntityDropParamsAccess access) || access.enchiridion$getPostEntityDropsParams() == null)
            return original;
        access.enchiridion$getPostEntityDropsParams().withParameter(EnchiridionLootContextParams.ITEM_ENTITY, original);
        access.enchiridion$getPostEntityDropsParams().withParameter(LootContextParams.ORIGIN, original.position());

        Entity entity = access.enchiridion$getPostEntityDropsParams().getOptionalParameter(LootContextParams.ATTACKING_ENTITY);
        if (!(entity instanceof LivingEntity attacker))
            return original;

        for (Object2IntMap.Entry<Holder<Enchantment>> enchantment : attacker.getMainHandItem().getEnchantments().entrySet().stream().filter(entry -> entry.getKey().isBound() && !entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.POST_ENTITY_DROP).isEmpty()).toList()) {
            access.enchiridion$getPostEntityDropsParams().withParameter(LootContextParams.ENCHANTMENT_LEVEL, enchantment.getIntValue());
            LootContext context = new LootContext.Builder(access.enchiridion$getPostEntityDropsParams().create(EnchiridionLootContextParamSets.ENCHANTED_ENTITY_DROP)).create(Optional.empty());
            for (ConditionalEffect<EnchantmentEntityEffect> effect : enchantment.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.POST_ENTITY_DROP)) {
                if (effect.matches(context)) {
                    effect.effect().apply(serverLevel, context.getParam(LootContextParams.ENCHANTMENT_LEVEL), new EnchantedItemInUse(attacker.getMainHandItem(), EquipmentSlot.MAINHAND, attacker), original, original.position());
                }
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "isFreezing", at = @At("RETURN"))
    private boolean enchiridion$setFreezingWhenEnchantment(boolean original) {
        return original || Enchiridion.getHelper().isFrozenByEnchantment((Entity)(Object)this) && canFreeze();
    }

    @Unique
    private List<Triple<EquipmentSlot, Integer, RidingConditionalEffect<EnchantmentLocationBasedEffect>>> enchiridion$activeRidingEffects = new ArrayList<>();

    @ModifyReturnValue(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At("RETURN"))
    private boolean enchiridion$performRidingEffects(boolean original, Entity vehicle) {
        if (!this.level().isClientSide() && (Entity)(Object)this instanceof LivingEntity living && original) {
            LootParams.Builder params = new LootParams.Builder((ServerLevel) living.level());
            params.withParameter(LootContextParams.THIS_ENTITY, living);
            params.withParameter(EnchiridionLootContextParams.VEHICLE, vehicle);
            params.withParameter(LootContextParams.ORIGIN, living.position());
            params.withOptionalParameter(EnchiridionLootContextParams.FIRST_PASSENGER, living.getFirstPassenger());

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                for (Object2IntMap.Entry<Holder<Enchantment>> enchantment : living.getItemBySlot(slot).getEnchantments().entrySet().stream().filter(entry -> entry.getKey().isBound() && entry.getKey().value().matchingSlot(slot) && !entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.VEHICLE_CHANGED).isEmpty()).toList()) {
                    params.withParameter(LootContextParams.ENCHANTMENT_LEVEL, enchantment.getIntValue());
                    for (RidingConditionalEffect<EnchantmentLocationBasedEffect> conditionalEffect : enchantment.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.VEHICLE_CHANGED)) {
                        if (conditionalEffect.matches(new LootContext.Builder(params.create(EnchiridionLootContextParamSets.VEHICLE_ENCHANTED)).create(Optional.empty()))) {
                            EnchantedItemInUse itemInUse = new EnchantedItemInUse(living.getItemBySlot(slot), slot, living);
                            for (Entity entity : conditionalEffect.affected().getEntities(living)) {
                                conditionalEffect.effect().onChangedBlock(
                                        (ServerLevel) living.level(),
                                        enchantment.getIntValue(),
                                        itemInUse,
                                        entity,
                                        entity.position(),
                                        !enchiridion$activeRidingEffects.contains(Triple.of(slot, enchantment.getIntValue(), conditionalEffect)));
                            }
                            enchiridion$addRidingEffect(slot, enchantment.getIntValue(), conditionalEffect);
                        }
                    }
                }
            }
        }
        return original;
    }

    @Inject(method = "removeVehicle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;removePassenger(Lnet/minecraft/world/entity/Entity;)V"))
    private void enchiridion$removeRidingEffects(CallbackInfo ci) {
        enchiridion$resetRidingEffects();
    }

    @Override
    public void enchiridion$addRidingEffect(EquipmentSlot slot, int level, RidingConditionalEffect<EnchantmentLocationBasedEffect> effect) {
        enchiridion$activeRidingEffects.add(Triple.of(slot, level, effect));
    }

    @Override
    public void enchiridion$resetRidingEffects() {
        if (!((Entity)(Object)this instanceof LivingEntity living) || living.level().isClientSide())
            return;
        for (Triple<EquipmentSlot, Integer, RidingConditionalEffect<EnchantmentLocationBasedEffect>> effect : enchiridion$activeRidingEffects) {
            for (Entity entity : effect.getRight().affected().getEntities((Entity)(Object)this)) {
                EnchantedItemInUse itemInUse = new EnchantedItemInUse(living.getItemBySlot(effect.getLeft()), effect.getLeft(), living);
                effect.getRight().effect().onDeactivated(itemInUse, entity, entity.position(), effect.getMiddle());
            }
        }
        enchiridion$activeRidingEffects.clear();
    }
}
