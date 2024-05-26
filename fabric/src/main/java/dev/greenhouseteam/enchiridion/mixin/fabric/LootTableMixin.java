package dev.greenhouseteam.enchiridion.mixin.fabric;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.mixin.LootParamsAccessor;
import dev.greenhouseteam.enchiridion.registry.EnchiridionEnchantmentEffectComponents;
import dev.greenhouseteam.enchiridion.registry.EnchiridionLootContextParamSets;
import dev.greenhouseteam.enchiridion.registry.EnchiridionLootContextParams;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Optional;

@Mixin(LootTable.class)
public class LootTableMixin {
    @Unique
    private static ResourceKey<LootTable> enchiridion$previousTable;

    @ModifyReturnValue(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"))
    private ObjectArrayList<ItemStack> enchiridion$modifyItemsForLootTable(ObjectArrayList<ItemStack> original, LootParams params) {
        LootParams.Builder paramBuilder = new LootParams.Builder(params.getLevel());
        for (Map.Entry<LootContextParam<?>, Object> param : ((LootParamsAccessor)params).enchiridion$getParams().entrySet())
            paramBuilder.withParameter((LootContextParam<Object>) param.getKey(), param.getValue());
        if (params.hasParam(LootContextParams.TOOL)) {
            EquipmentSlot slot = null;
            LivingEntity living = null;
            if (params.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof FishingHook hook)
                living = hook.getPlayerOwner();
            else if (params.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity entity)
                living = entity;
            if (living != null) {
                for (EquipmentSlot slot1 : EquipmentSlot.values()) {
                    if (living.getItemBySlot(slot1).equals(params.getParameter(LootContextParams.TOOL))) {
                        slot = slot1;
                        paramBuilder.withParameter(EnchiridionLootContextParams.EQUIPMENT_SLOT, slot);
                        break;
                    }
                }
            }
            EquipmentSlot finalSlot = slot;
            for (Map.Entry<Holder<Enchantment>, Integer> entry : params.getParameter(LootContextParams.TOOL).getEnchantments().entrySet().stream().filter(entry -> entry.getKey().isBound() && !entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.ADDITIONAL_FISHING_LOOT).isEmpty() && (finalSlot == null && !params.hasParam(LootContextParams.THIS_ENTITY) || entry.getKey().value().matchingSlot(finalSlot))).toList()) {
                paramBuilder.withOptionalParameter(LootContextParams.ENCHANTMENT_LEVEL, entry.getValue());
                LootParams params2 = paramBuilder.create(EnchiridionLootContextParamSets.ENCHANTED_FISHING);
                LootContext context = new LootContext.Builder(params2).create(Optional.empty());
                for (ConditionalEffect<ResourceKey<LootTable>> effect : entry.getKey().value().getEffects(EnchiridionEnchantmentEffectComponents.ADDITIONAL_FISHING_LOOT)) {
                    if (effect.effect() == enchiridion$previousTable)
                        continue;
                    if (!effect.matches(context))
                        continue;
                    original.addAll(context.getLevel().getServer().reloadableRegistries().getLootTable(effect.effect()).getRandomItems(params2, params2.getLevel().getRandom()));
                    enchiridion$previousTable = effect.effect();
                }
            }
        }
        enchiridion$previousTable = null;

        return original;
    }
}
