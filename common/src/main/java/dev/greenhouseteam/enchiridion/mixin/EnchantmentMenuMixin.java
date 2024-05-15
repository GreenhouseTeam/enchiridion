package dev.greenhouseteam.enchiridion.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.greenhouseteam.enchiridion.access.UpgradeableEnchantmentMenuAccess;
import dev.greenhouseteam.enchiridion.gui.ScrollIndexDataSlot;
import dev.greenhouseteam.enchiridion.util.EnchantingTableUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.EnchantingTableBlock;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin  extends AbstractContainerMenu implements UpgradeableEnchantmentMenuAccess {
    @Shadow @Final private ContainerLevelAccess access;

    @Shadow @Final public int[] costs;

    @Shadow @Final public int[] enchantClue;

    @Shadow @Final public int[] levelClue;

    @Shadow @Final private RandomSource random;

    @Shadow @Final private DataSlot enchantmentSeed;

    @Shadow @Final private Container enchantSlots;

    @Unique
    private ScrollIndexDataSlot enchiridion$scrollIndex;

    @Unique
    private final DataSlot enchiridion$levelableEnchantmentSize = DataSlot.standalone();

    @Unique
    private final int[] enchiridion$requiredBookshelves = new int[]{-1, -1, -1};

    @Unique
    private final int[] enchiridion$bookshelfCount = new int[]{-1, -1, -1};

    protected EnchantmentMenuMixin(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void enchiridion$addDataSlotsToEnchantingTableMenu(int containerId, Inventory inventory, ContainerLevelAccess level, CallbackInfo ci) {
        enchiridion$scrollIndex = new ScrollIndexDataSlot(enchantSlots);
        this.addDataSlot(enchiridion$scrollIndex).set(-1);
        this.addDataSlot(enchiridion$levelableEnchantmentSize).set(-1);
        this.addDataSlot(DataSlot.shared(enchiridion$bookshelfCount, 0));
        this.addDataSlot(DataSlot.shared(enchiridion$bookshelfCount, 1));
        this.addDataSlot(DataSlot.shared(enchiridion$bookshelfCount, 2));
        this.addDataSlot(DataSlot.shared(enchiridion$requiredBookshelves, 0));
        this.addDataSlot(DataSlot.shared(enchiridion$requiredBookshelves, 1));
        this.addDataSlot(DataSlot.shared(enchiridion$requiredBookshelves, 2));
    }

    @Inject(method = "slotsChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void enchiridion$allowEnchantmentLevelUps(Container container, CallbackInfo ci, ItemStack stack) {
        if (!stack.isEmpty() && stack.isEnchanted()) {
            enchiridion$levelableEnchantmentSize.set(0);
            enchiridion$scrollIndex.set(0);
            access.execute((level, blockPos) -> {
                List<Object2IntMap.Entry<Holder<Enchantment>>> enchantmentSet = stack.getEnchantments().entrySet().stream().toList();
                int i = 0;

                for (BlockPos blockPos2 : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
                    if (EnchantingTableBlock.isValidBookShelf(level, blockPos, blockPos2)) {
                        ++i;
                    }
                }

                this.random.setSeed(enchantmentSeed.get());

                int j;
                for (j = 0; j < 3; ++j) {
                    this.costs[j] = 0;
                    this.enchantClue[j] = -1;
                    this.levelClue[j] = -1;
                    enchiridion$requiredBookshelves[j] = -1;
                    enchiridion$bookshelfCount[j] = -1;
                }

                j = 0;
                int size = 0;
                for (Object2IntMap.Entry<Holder<Enchantment>> holder : enchantmentSet) {
                    int maxLevel = holder.getKey().value().getMaxLevel();
                    if (holder.getIntValue() < maxLevel) {
                        ++size;
                        if (j < 3) {
                            this.costs[j] = EnchantingTableUtil.getLevelUpCost(holder.getIntValue() + 1, maxLevel, stack);
                            this.enchantClue[j] = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getId(holder.getKey().value());
                            this.levelClue[j] = holder.getIntValue() + 1;
                            int bookshelfCount = EnchantingTableUtil.getMinimumBookshelfAmountForLevelling(holder.getIntValue() + 1, maxLevel);
                            enchiridion$requiredBookshelves[j] = bookshelfCount;
                            enchiridion$bookshelfCount[j] = i;
                            ++j;
                        }
                    }
                }
                enchiridion$levelableEnchantmentSize.set(size);
            });
            this.broadcastChanges();
            ci.cancel();
        } else {
            enchiridion$levelableEnchantmentSize.set(-1);
            for(int j = 0; j < 3; ++j) {
                enchiridion$requiredBookshelves[j] = -1;
                enchiridion$bookshelfCount[j] = -1;
            }
        }
    }

    @ModifyVariable(method = "clickMenuButton", at = @At(value = "LOAD"), ordinal = 1)
    private int enchiridion$allowEnchantmentLevelUps(int original, Player player, int index, @Local(ordinal = 0) ItemStack stack) {
        if (!stack.isEmpty() && stack.isEnchanted()) {
            Holder<Enchantment> enchantment = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap().byId(enchantClue[index]);
            if (enchantment != null && enchantment.isBound())
                return EnchantingTableUtil.getLapisCountForLevelling(levelClue[index], enchantment.value().getMaxLevel());
        }
        return original;
    }

    @Inject(method = "clickMenuButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V"), cancellable = true)
    private void enchiridion$allowEnchantmentLevelUps(Player player, int index, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) ItemStack stack, @Local(ordinal = 1) ItemStack lapis, @Local(ordinal = 1) int lapisCount) {
        if (!stack.isEmpty() && stack.isEnchanted()) {
            access.execute((level, blockPos) -> {
                Holder<Enchantment> enchantment = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap().byId(enchantClue[index]);
                if (enchantment != null && enchantment.isBound()) {

                    player.onEnchantmentPerformed(stack, EnchantingTableUtil.getLapisCountForLevelling(levelClue[index], enchantment.value().getMaxLevel()));
                    stack.enchant(enchantment, levelClue[index]);

                    lapis.consume(lapisCount, player);
                    if (lapis.isEmpty()) {
                        this.enchantSlots.setItem(1, ItemStack.EMPTY);
                    }

                    player.awardStat(Stats.ENCHANT_ITEM);
                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, stack, lapisCount);
                    }

                    enchantSlots.setChanged();
                    enchantmentSeed.set(player.getEnchantmentSeed());
                    slotsChanged(this.enchantSlots);

                    level.playSound(null, blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);

                    if (enchiridion$scrollIndex.get() > Math.max(enchiridion$levelableEnchantmentSize.get() - 3, 0))
                        enchiridion$scrollIndex.set(Math.max(enchiridion$levelableEnchantmentSize.get() - 3, 0));

                    this.broadcastChanges();

                    cir.setReturnValue(true);
                } else {
                    cir.setReturnValue(false);
                }
            });
        }
    }

    @Override
    public int enchiridion$getScrollIndex() {
        return enchiridion$scrollIndex.get();
    }

    @Override
    public void enchiridion$setScrollIndex(int value) {
        enchiridion$scrollIndex.set(value);
    }

    @Override
    public int enchiridion$getBookshelfCount(int index) {
        return enchiridion$bookshelfCount[index];
    }

    @Override
    public int enchiridion$getRequiredBookshelves(int index) {
        return enchiridion$requiredBookshelves[index];
    }
}
