package dev.greenhouseteam.enchiridion.mixin;

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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
import java.util.function.Supplier;

@Mixin(Block.class)
public class BlockMixin {
    private static LootParams.Builder enchiridion$builder;

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    private static void enchiridion$captureItemStack(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack stack, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            LootParams.Builder builder = new LootParams.Builder(serverLevel);
            builder.withParameter(LootContextParams.TOOL, stack);
            builder.withParameter(LootContextParams.BLOCK_STATE, state);
            builder.withOptionalParameter(LootContextParams.THIS_ENTITY, entity);
            builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
            enchiridion$builder = builder;
        }
    }

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private static void enchiridion$resetBuilderLocal(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack stack, CallbackInfo ci) {
        enchiridion$builder = null;
    }

    @Inject(method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void enchiridion$postBlockDropEffectComponents(Level level, Supplier<ItemEntity> supplier, ItemStack stack, CallbackInfo ci, ItemEntity itemEntity) {
        if (!(level instanceof ServerLevel serverLevel))
            return;
        if (enchiridion$builder == null)
            return;
        LootParams.Builder builder = enchiridion$builder;
        builder.withParameter(EnchiridionLootContextParams.ITEM_ENTITY, itemEntity);
        LootContext params1 = new LootContext.Builder(builder.create(EnchiridionLootContextParamSets.BLOCK_DROP)).create(Optional.empty());

        ItemStack tool = params1.getParam(LootContextParams.TOOL);
        for (Object2IntMap.Entry<Holder<Enchantment>> enchantment : tool.getEnchantments().entrySet()) {
            if (enchantment.getKey().isBound())
                for (ConditionalEffect<EnchantmentEntityEffect> effect : enchantment.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.POST_BLOCK_DROP)) {
                    if (effect.matches(params1)) {
                        LivingEntity living;
                        if (params1.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity paramLiving)
                            living = paramLiving;
                        else
                            living = null;
                        effect.effect().apply(serverLevel, enchantment.getIntValue(), new EnchantedItemInUse(params1.getParam(LootContextParams.TOOL), EquipmentSlot.MAINHAND, living, () -> {
                            if (living != null) {
                                living.broadcastBreakEvent(EquipmentSlot.MAINHAND);
                            }
                        }), itemEntity, itemEntity.position());
                    }
                }
        }
    }
}
