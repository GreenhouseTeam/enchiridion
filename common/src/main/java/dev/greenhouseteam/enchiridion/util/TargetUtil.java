package dev.greenhouseteam.enchiridion.util;

import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.access.PlayerTargetAccess;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionLootContextParamSets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TargetUtil {
    public static void updateBlockLookEffects(ServerPlayer player) {
        var miningEnchantmentComponents = player.getMainHandItem().getEnchantments().entrySet().stream().filter(entry -> entry.getKey().isBound() && !entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.TARGET_BLOCK_CHANGED).isEmpty()).map(entry -> Triple.of(entry.getKey(), entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.TARGET_BLOCK_CHANGED), entry.getIntValue())).toList();
        boolean changedAttributes = false;

        if (miningEnchantmentComponents.isEmpty() && ((PlayerTargetAccess)player).enchiridion$activeBlockTargetEnchantmentEffects().isEmpty())
            return;

        Vec3 startPos = player.getEyePosition();
        Vec3 endPos = startPos.add(player.calculateViewVector(player.getXRot(), player.getYRot()).scale(player.blockInteractionRange()));
        BlockHitResult result = player.level().clip(new ClipContext(startPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        BlockPos pos = result.getBlockPos();

        if (!miningEnchantmentComponents.isEmpty()) {
            LootParams.Builder params = new LootParams.Builder(player.serverLevel());
            params.withParameter(LootContextParams.BLOCK_STATE, player.level().getBlockState(pos));
            params.withParameter(LootContextParams.ORIGIN, pos.getCenter());
            params.withParameter(LootContextParams.TOOL, player.getMainHandItem());
            params.withParameter(LootContextParams.THIS_ENTITY, player);
            params.withOptionalParameter(LootContextParams.BLOCK_ENTITY, player.level().getBlockEntity(pos));
            for (Triple<Holder<Enchantment>, List<ConditionalEffect<EnchantmentLocationBasedEffect>>, Integer> entry : miningEnchantmentComponents) {
                params.withParameter(LootContextParams.ENCHANTMENT_LEVEL, entry.getRight());
                LootContext context = new LootContext.Builder(params.create(EnchiridionLootContextParamSets.ENCHANTED_BLOCK)).create(Optional.empty());
                for (ConditionalEffect<EnchantmentLocationBasedEffect> effect : entry.getMiddle()) {
                    if (effect.matches(context)) {;
                        effect.effect().onChangedBlock(
                                (ServerLevel) player.level(),
                                entry.getRight(),
                                new EnchantedItemInUse(player.getMainHandItem(), EquipmentSlot.MAINHAND, player),
                                player,
                                pos.getCenter(),
                                !((PlayerTargetAccess)player).enchiridion$activeBlockTargetEnchantmentEffects().containsKey(entry.getLeft()) || !((PlayerTargetAccess)player).enchiridion$activeBlockTargetEnchantmentEffects().get(entry.getLeft()).contains(effect.effect())
                        );
                        if (!changedAttributes)
                            changedAttributes = effect.effect() instanceof EnchantmentAttributeEffect && !((PlayerTargetAccess)player).enchiridion$activeBlockTargetEnchantmentEffects().containsKey(entry.getLeft()) || !((PlayerTargetAccess)player).enchiridion$activeBlockTargetEnchantmentEffects().get(entry.getLeft()).contains(effect.effect());
                        ((PlayerTargetAccess)player).enchiridion$addActiveBlockTargetEnchantmentEffect(entry.getLeft(), effect.effect());

                    } else if (((PlayerTargetAccess)player).enchiridion$activeBlockTargetEnchantmentEffects().containsKey(entry.getLeft()) && ((PlayerTargetAccess)player).enchiridion$activeBlockTargetEnchantmentEffects().get(entry.getLeft()).contains(effect.effect())) {
                            ((PlayerTargetAccess)player).enchiridion$removeActiveBlockTargetEnchantmentEffect(entry.getLeft(), effect.effect());
                            effect.effect().onDeactivated(
                                new EnchantedItemInUse(player.getMainHandItem(), EquipmentSlot.MAINHAND, player),
                                player,
                                pos.getCenter(),
                                entry.getRight()
                        );
                        if (!changedAttributes)
                            changedAttributes = effect.effect() instanceof EnchantmentAttributeEffect;
                    }
                }
            }
        } else if (!((PlayerTargetAccess)player).enchiridion$activeBlockTargetEnchantmentEffects().isEmpty()) {
            for (Holder<Enchantment> holder : ((PlayerTargetAccess)player).enchiridion$activeBlockTargetEnchantmentEffects().keySet()) {
                Set<EnchantmentLocationBasedEffect> effects = ((PlayerTargetAccess)player).enchiridion$activeBlockTargetEnchantmentEffects().remove(holder);
                if (effects != null)
                    for (EnchantmentLocationBasedEffect effect : effects) {
                        effect.onDeactivated(
                                new EnchantedItemInUse(player.getMainHandItem(), EquipmentSlot.MAINHAND, player),
                                player,
                                pos.getCenter(),
                                player.getMainHandItem().getEnchantments().getLevel(holder)
                        );
                        if (!changedAttributes)
                            changedAttributes = effect instanceof EnchantmentAttributeEffect;
                    }
            }
        }

        if (changedAttributes) {
            ClientboundUpdateAttributesPacket packet = new ClientboundUpdateAttributesPacket(player.getId(), player.getAttributes().getAttributesToSync());
            for (ServerPlayer others : Enchiridion.getHelper().getTracking(player))
                others.connection.send(packet);
            player.connection.send(packet);
            player.getAttributes().getAttributesToSync().clear();
        }
    }

}
