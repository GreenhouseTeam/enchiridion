package dev.greenhouseteam.enchiridion.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.greenhouseteam.enchiridion.access.EntityPostEntityDropParamsAccess;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract Level level();

    @ModifyReturnValue(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "RETURN", ordinal = 2))
    private ItemEntity enchiridion$postEntityDropEffectComponents(ItemEntity original) {
        if (!(this.level() instanceof ServerLevel serverLevel) || !((Entity)(Object)this instanceof EntityPostEntityDropParamsAccess access) || access.enchiridion$getPostEntityDropsParams() == null)
            return original;
        access.enchiridion$getPostEntityDropsParams().withParameter(EnchiridionLootContextParams.ITEM_ENTITY, original);
        access.enchiridion$getPostEntityDropsParams().withParameter(LootContextParams.ORIGIN, original.position());

        Entity entity = access.enchiridion$getPostEntityDropsParams().getOptionalParameter(LootContextParams.ATTACKING_ENTITY);
        if (!(entity instanceof LivingEntity attacker))
            return original;

        for (Object2IntMap.Entry<Holder<Enchantment>> enchantment : attacker.getMainHandItem().getEnchantments().entrySet()) {
            if (enchantment.getKey().isBound()) {
                access.enchiridion$getPostEntityDropsParams().withParameter(LootContextParams.ENCHANTMENT_LEVEL, enchantment.getIntValue());
                LootContext context = new LootContext.Builder(access.enchiridion$getPostEntityDropsParams().create(EnchiridionLootContextParamSets.ENCHANTED_ENTITY_DROP)).create(Optional.empty());
                for (ConditionalEffect<EnchantmentEntityEffect> effect : enchantment.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.POST_ENTITY_DROP)) {
                    if (effect.matches(context)) {
                        effect.effect().apply(serverLevel, context.getParam(LootContextParams.ENCHANTMENT_LEVEL), new EnchantedItemInUse(attacker.getMainHandItem(), EquipmentSlot.MAINHAND, attacker), original, original.position());
                    }
                }
            }
        }
        return original;
    }
}
