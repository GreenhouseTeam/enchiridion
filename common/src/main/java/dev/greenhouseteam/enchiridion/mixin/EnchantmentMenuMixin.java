package dev.greenhouseteam.enchiridion.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.greenhouseteam.enchiridion.access.LevelUpEnchantmentMenuAccess;
import dev.greenhouseteam.enchiridion.enchantment.category.ItemEnchantmentCategories;
import dev.greenhouseteam.enchiridion.registry.EnchiridionDataComponents;
import dev.greenhouseteam.enchiridion.util.EnchantingTableUtil;
import dev.greenhouseteam.enchiridion.util.EnchiridionUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
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

import java.util.ArrayList;
import java.util.List;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin  extends AbstractContainerMenu implements LevelUpEnchantmentMenuAccess {
    @Shadow @Final private ContainerLevelAccess access;

    @Shadow @Final public int[] costs;

    @Shadow @Final public int[] enchantClue;

    @Shadow @Final public int[] levelClue;

    @Shadow @Final private RandomSource random;

    @Shadow @Final private DataSlot enchantmentSeed;

    @Shadow @Final private Container enchantSlots;

    @Unique
    private final DataSlot enchiridion$levelableEnchantmentSize = DataSlot.standalone();

    @Unique
    private final DataSlot enchiridion$bookshelfCount = DataSlot.standalone();

    @Unique
    private final int[] enchiridion$requiredBookshelves = new int[]{-1, -1, -1};

    @Unique
    private List<Integer> enchiridion$allLevelUpCosts = new ArrayList<>();

    @Unique
    private List<Integer> enchiridion$allEnchantClues = new ArrayList<>();

    @Unique
    private List<Integer> enchiridion$allLevelClues = new ArrayList<>();

    @Unique
    private List<Integer> enchiridion$allRequiredBookshelves = new ArrayList<>();

    @Unique
    private double enchiridion$scrollOff = 0;

    protected EnchantmentMenuMixin(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void enchiridion$addDataSlotsToEnchantingTableMenu(int containerId, Inventory inventory, ContainerLevelAccess level, CallbackInfo ci) {
        this.addDataSlot(enchiridion$levelableEnchantmentSize).set(-1);
        this.addDataSlot(enchiridion$bookshelfCount).set(-1);
        this.addDataSlot(DataSlot.shared(enchiridion$requiredBookshelves, 0));
        this.addDataSlot(DataSlot.shared(enchiridion$requiredBookshelves, 1));
        this.addDataSlot(DataSlot.shared(enchiridion$requiredBookshelves, 2));
    }

    @Inject(method = "slotsChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void enchiridion$allowEnchantmentLevelUps(Container container, CallbackInfo ci, ItemStack stack) {
        enchiridion$allLevelUpCosts.clear();
        enchiridion$allEnchantClues.clear();
        enchiridion$allLevelClues.clear();
        enchiridion$allRequiredBookshelves.clear();
        if (!stack.isEmpty() && stack.isEnchanted()) {
            enchiridion$levelableEnchantmentSize.set(0);
            access.execute((level, blockPos) -> {
                List<Object2IntMap.Entry<Holder<Enchantment>>> enchantmentSet = new ArrayList<>(stack.getEnchantments().entrySet().stream().toList());
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
                }

                int scrollOffset =  Mth.floor(enchiridion$scrollOff);
                j = Mth.floor(enchiridion$scrollOff);
                int size = 0;
                enchantmentSet.sort((o1, o2) -> EnchiridionUtil.compareEnchantments(o1.getKey(), o2.getKey(), stack.getOrDefault(EnchiridionDataComponents.ENCHANTMENT_CATEGORIES, ItemEnchantmentCategories.EMPTY)));
                for (Object2IntMap.Entry<Holder<Enchantment>> holder : enchantmentSet) {
                    int maxLevel = holder.getKey().value().getMaxLevel();
                    if (holder.getIntValue() < maxLevel) {
                        int cost = EnchantingTableUtil.getLevelUpCost(holder.getIntValue() + 1, maxLevel, stack);
                        int enchantClue = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getId(holder.getKey().value());
                        int levelClue = holder.getIntValue() + 1;
                        int bookshelfCount = EnchantingTableUtil.getMinimumBookshelfAmountForLevelling(holder.getIntValue() + 1, maxLevel);
                        enchiridion$allLevelUpCosts.add(cost);
                        enchiridion$allEnchantClues.add(enchantClue);
                        enchiridion$allLevelClues.add(levelClue);
                        enchiridion$allRequiredBookshelves.add(levelClue);
                        ++size;
                        if (j < 3 + scrollOffset && j - scrollOffset >= 0) {
                            this.costs[j - scrollOffset] = cost;
                            this.enchantClue[j - scrollOffset] = enchantClue;
                            this.levelClue[j - scrollOffset] = levelClue;
                            enchiridion$requiredBookshelves[j - scrollOffset] = bookshelfCount;
                            ++j;
                        }
                    }
                }
                if (size > 0)
                    enchiridion$bookshelfCount.set(i);

                if (size > 3 && scrollOffset > size - 3)
                    enchiridion$scrollOff = size - 3;
                enchiridion$levelableEnchantmentSize.set(size);
            });
            this.broadcastChanges();
            ci.cancel();
        } else {
            enchiridion$levelableEnchantmentSize.set(-1);
            enchiridion$bookshelfCount.set(-1);
            for(int j = 0; j < 3; ++j) {
                enchiridion$requiredBookshelves[j] = -1;
                enchiridion$scrollOff = 0;
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
                    this.broadcastChanges();

                    cir.setReturnValue(true);
                } else {
                    cir.setReturnValue(false);
                }
            });
        }
    }

    @Override
    public int enchiridion$getEnchantmentSize() {
        return enchiridion$levelableEnchantmentSize.get();
    }

    @Override
    public int enchiridion$getBookshelfCount() {
        return enchiridion$bookshelfCount.get();
    }

    @Override
    public int enchiridion$getRequiredBookshelves(int index) {
        return enchiridion$requiredBookshelves[index];
    }

    @Override
    public double enchiridion$getScrollOffset() {
        return enchiridion$scrollOff;
    }

    public void enchiridion$setScrollOffset(double value) {
        enchiridion$scrollOff = value;
    }

    @Override
    public void enchiridion$refreshEnchantmentIndexes() {
        int scrollOff = Mth.floor(enchiridion$scrollOff);
        for (int i = scrollOff; i < 3 + scrollOff; ++i) {
            this.costs[i - scrollOff] = enchiridion$allLevelUpCosts.get(i);
            this.enchantClue[i - scrollOff] = enchiridion$allEnchantClues.get(i);
            this.levelClue[i - scrollOff] = enchiridion$allLevelClues.get(i);
            enchiridion$requiredBookshelves[i - scrollOff] = enchiridion$allRequiredBookshelves.get(i);
        }
    }
}
