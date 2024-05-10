package dev.greenhouseteam.enchiridion.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.sun.jna.platform.unix.aix.Perfstat;
import dev.greenhouseteam.enchiridion.enchantment.effects.PreventHungerConsumptionEffect;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionLootContextParamSets;
import dev.greenhouseteam.enchiridion.registry.EnchiridionLootContextParams;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
import java.util.function.Supplier;

@Mixin(Block.class)
public class BlockMixin {
    @Unique
    private static LootParams.Builder enchiridion$builder;

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    private static void enchiridion$captureBlockDropBuilder(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack stack, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            enchiridion$builder = new LootParams.Builder(serverLevel);
            enchiridion$builder.withParameter(LootContextParams.TOOL, stack);
            enchiridion$builder.withParameter(LootContextParams.BLOCK_STATE, state);
            enchiridion$builder.withOptionalParameter(LootContextParams.THIS_ENTITY, entity);
            enchiridion$builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
        }
    }

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private static void enchiridion$resetBlockBuilder(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack stack, CallbackInfo ci) {
        enchiridion$builder = null;
    }

    @Inject(method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void enchiridion$postBlockDropEffectComponents(Level level, Supplier<ItemEntity> supplier, ItemStack stack, CallbackInfo ci, ItemEntity itemEntity) {
        if (!(level instanceof ServerLevel serverLevel) || enchiridion$builder == null)
            return;
        enchiridion$builder.withParameter(EnchiridionLootContextParams.ITEM_ENTITY, itemEntity);
        enchiridion$builder.withParameter(LootContextParams.ORIGIN, itemEntity.position());

        ItemStack tool = enchiridion$builder.getParameter(LootContextParams.TOOL);
        for (Object2IntMap.Entry<Holder<Enchantment>> enchantment : tool.getEnchantments().entrySet().stream().filter(entry -> entry.getKey().isBound() && !entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.POST_BLOCK_DROP).isEmpty()).toList()) {
            enchiridion$builder.withParameter(LootContextParams.ENCHANTMENT_LEVEL, enchantment.getIntValue());
            LootContext context = new LootContext.Builder(enchiridion$builder.create(EnchiridionLootContextParamSets.ENCHANTED_BLOCK_DROP)).create(Optional.empty());
            for (ConditionalEffect<EnchantmentEntityEffect> effect : enchantment.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.POST_BLOCK_DROP)) {
                if (effect.matches(context)) {
                    LivingEntity living;
                    if (context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity paramLiving)
                        living = paramLiving;
                    else
                        living = null;
                    effect.effect().apply(serverLevel, context.getParam(LootContextParams.ENCHANTMENT_LEVEL), new EnchantedItemInUse(context.getParam(LootContextParams.TOOL), EquipmentSlot.MAINHAND, living, () -> {
                        if (living != null)
                            living.broadcastBreakEvent(EquipmentSlot.MAINHAND);
                    }), itemEntity, itemEntity.position());
                }
            }
        }
    }

    @WrapWithCondition(method = "playerDestroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"))
    private boolean enchiridion$wrapExhaustionFromMining(Player player, float value, @Local(argsOnly = true) BlockPos pos,  @Local(argsOnly = true) ItemStack stack) {
        return !PreventHungerConsumptionEffect.shouldPreventMiningConsumption(stack, player, pos);
    }
}
