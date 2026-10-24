package com.lum.pocket_jukebox.world.item;

import com.lum.pocket_jukebox.component.ModDataComponents;
import com.lum.pocket_jukebox.inventory.tooltip.PocketJukeboxTooltip;
import com.lum.pocket_jukebox.world.item.component.PocketJukeboxContents;
import com.mojang.serialization.DataResult;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class PocketJukeboxItem extends Item {
    public PocketJukeboxItem(Properties properties) {
        super(properties);
    }
    private static final int FULL_BAR_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);
    private static final int BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);

    private static Fraction getWeightSafe(final PocketJukeboxContents contents) {
        return switch (contents.weight()) {
            case DataResult.Success<Fraction> success -> (Fraction)success.value();
            case DataResult.Error<?> error -> Fraction.ONE;
            default -> throw new MatchException(null, null);
        };
    }

    public static float getFullnessDisplay(final ItemStack itemStack) {
        PocketJukeboxContents contents = itemStack.getOrDefault(ModDataComponents.POCKET_JUKEBOX_CONTENTS, PocketJukeboxContents.EMPTY);
        return getWeightSafe(contents).floatValue();
    }

    @Override
    public boolean overrideStackedOnOther(final ItemStack self, final Slot slot, final ClickAction clickAction, final Player player) {
        PocketJukeboxContents initialContents = self.get(ModDataComponents.POCKET_JUKEBOX_CONTENTS);
        if (initialContents == null) {
            return false;
        }

        ItemStack other = slot.getItem();
        PocketJukeboxContents.Mutable contents = initialContents.asMutable();
        if (clickAction == ClickAction.PRIMARY && !other.isEmpty()) {
            if (contents.tryTransfer(slot, player) > 0) {
                playInsertSound(player);
            } else {
                playInsertFailSound(player);
            }

            self.set(ModDataComponents.POCKET_JUKEBOX_CONTENTS, contents.toImmutable());
            this.broadcastChangesOnContainerMenu(player);
            return true;
        } else if (clickAction == ClickAction.SECONDARY && other.isEmpty()) {
            ItemStack itemStack = contents.removeOne();
            if (itemStack != null) {
                ItemStack remainder = slot.safeInsert(itemStack);
                if (remainder.getCount() > 0) {
                    contents.tryInsert(remainder);
                } else {
                    playRemoveOneSound(player);
                }
            }

            self.set(ModDataComponents.POCKET_JUKEBOX_CONTENTS, contents.toImmutable());
            this.broadcastChangesOnContainerMenu(player);
            return true;
        } else {
            return false;
        }
    }

    private static void cycleDisc(final ItemStack stack, final PocketJukeboxContents contents) {
        int current = Math.max(contents.getSelectedItemIndex(), 0);
        int next = (current + 1) % contents.size();
        if (next != contents.getSelectedItemIndex()) {
            toggleSelectedItem(stack, next);
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            final ItemStack self, final ItemStack other, final Slot slot, final ClickAction clickAction, final Player player, final SlotAccess carriedItem
    ) {
        if (clickAction == ClickAction.PRIMARY && other.isEmpty()) {
            toggleSelectedItem(self, -1);
            return false;
        }

        PocketJukeboxContents initialContents = self.get(ModDataComponents.POCKET_JUKEBOX_CONTENTS);
        if (initialContents == null) {
            return false;
        }

        PocketJukeboxContents.Mutable contents = initialContents.asMutable();
        if (clickAction == ClickAction.PRIMARY && !other.isEmpty()) {
            if (slot.allowModification(player) && contents.tryInsert(other) > 0) {
                playInsertSound(player);
            } else {
                playInsertFailSound(player);
            }

            self.set(ModDataComponents.POCKET_JUKEBOX_CONTENTS, contents.toImmutable());
            this.broadcastChangesOnContainerMenu(player);
            return true;
        } else if (clickAction == ClickAction.SECONDARY && other.isEmpty()) {
            if (slot.allowModification(player)) {
                ItemStack removed = contents.removeOne();
                if (removed != null) {
                    playRemoveOneSound(player);
                    carriedItem.set(removed);
                }
            }

            self.set(ModDataComponents.POCKET_JUKEBOX_CONTENTS, contents.toImmutable());
            this.broadcastChangesOnContainerMenu(player);
            return true;
        } else {
            toggleSelectedItem(self, -1);
            return false;
        }
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        PocketJukeboxContents contents = stack.get(ModDataComponents.POCKET_JUKEBOX_CONTENTS);
        if (contents == null || contents.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            int current = Math.max(contents.getSelectedItemIndex(), 0);
            if (player.isShiftKeyDown()) {
                PocketJukeboxPlayback.play(serverLevel, serverPlayer, stack, current + 1);
            } else if (PocketJukeboxPlayback.isPlaying(player)) {
                PocketJukeboxPlayback.stop(serverPlayer, true);
            } else {
                PocketJukeboxPlayback.play(serverLevel, serverPlayer, stack, current);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void dropContent(final Level level, final Player player, final ItemStack itemStack) {
        if (this.dropContent(itemStack, player)) {
            playDropContentsSound(level, player);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        PocketJukeboxContents contents = stack.getOrDefault(ModDataComponents.POCKET_JUKEBOX_CONTENTS, PocketJukeboxContents.EMPTY);
        return getWeightSafe(contents).compareTo(Fraction.ZERO) > 0;
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        PocketJukeboxContents contents = stack.getOrDefault(ModDataComponents.POCKET_JUKEBOX_CONTENTS, PocketJukeboxContents.EMPTY);
        return Math.min(1 + Mth.mulAndTruncate(getWeightSafe(contents), 12), 13);
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        PocketJukeboxContents contents = stack.getOrDefault(ModDataComponents.POCKET_JUKEBOX_CONTENTS, PocketJukeboxContents.EMPTY);
        return getWeightSafe(contents).compareTo(Fraction.ONE) >= 0 ? FULL_BAR_COLOR : BAR_COLOR;
    }

    public static void toggleSelectedItem(final ItemStack stack, final int selectedItem) {
        PocketJukeboxContents initialContents = stack.get(ModDataComponents.POCKET_JUKEBOX_CONTENTS);
        if (initialContents != null) {
            PocketJukeboxContents.Mutable contents = initialContents.asMutable();
            contents.toggleSelectedItem(selectedItem);
            stack.set(ModDataComponents.POCKET_JUKEBOX_CONTENTS, contents.toImmutable());
        }
    }

    public static int getSelectedItemIndex(final ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.POCKET_JUKEBOX_CONTENTS, PocketJukeboxContents.EMPTY).getSelectedItemIndex();
    }

    public static @Nullable ItemStackTemplate getSelectedItem(final ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.POCKET_JUKEBOX_CONTENTS, PocketJukeboxContents.EMPTY).getSelectedItem();
    }

    public static int getNumberOfItemsToShow(final ItemStack stack) {
        PocketJukeboxContents contents = stack.getOrDefault(ModDataComponents.POCKET_JUKEBOX_CONTENTS, PocketJukeboxContents.EMPTY);
        return contents.getNumberOfItemsToShow();
    }

    private boolean dropContent(final ItemStack pocketJukebox, final Player player) {
        PocketJukeboxContents   contents = pocketJukebox.get(ModDataComponents.POCKET_JUKEBOX_CONTENTS);
        if (contents != null && !contents.isEmpty()) {
            Optional<ItemStack> itemStack = removeOneItemFromPocketJukebox(pocketJukebox, player, contents);
            if (itemStack.isPresent()) {
                player.drop(itemStack.get(), true, Prediction.PREDICTED);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static Optional<ItemStack> removeOneItemFromPocketJukebox(final ItemStack self, final Player player, final PocketJukeboxContents initialContents) {
        PocketJukeboxContents.Mutable contents = initialContents.asMutable();
        ItemStack removed = contents.removeOne();
        if (removed != null) {
            playRemoveOneSound(player);
            self.set(ModDataComponents.POCKET_JUKEBOX_CONTENTS, contents.toImmutable());
            return Optional.of(removed);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void onUseTick(final Level level, final LivingEntity livingEntity, final ItemStack itemStack, final int ticksRemaining) {
        if (livingEntity instanceof Player player) {
            int useDuration = this.getUseDuration(itemStack, livingEntity);
            boolean isFirstTick = ticksRemaining == useDuration;
            if (isFirstTick || ticksRemaining < useDuration - 10 && ticksRemaining % 2 == 0) {
                this.dropContent(level, player, itemStack);
            }
        }
    }

    @Override
    public int getUseDuration(final ItemStack itemStack, final LivingEntity entity) {
        return 200;
    }

    @Override
    public ItemUseAnimation getUseAnimation(final ItemStack itemStack) {
        return ItemUseAnimation.BUNDLE;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack pocketJukebox) {
        TooltipDisplay display = pocketJukebox.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        return !display.shows(ModDataComponents.POCKET_JUKEBOX_CONTENTS)
                ? Optional.empty()
                : Optional.ofNullable(pocketJukebox.get(ModDataComponents.POCKET_JUKEBOX_CONTENTS)).map(PocketJukeboxTooltip::new);
    }

    @Override
    public void onDestroyed(final ItemEntity entity) {
        PocketJukeboxContents contents = entity.getItem().get(ModDataComponents.POCKET_JUKEBOX_CONTENTS);
        if (contents != null) {
            entity.getItem().set(ModDataComponents.POCKET_JUKEBOX_CONTENTS, PocketJukeboxContents.EMPTY);
            ItemUtils.onContainerDestroyed(entity, contents.itemCopies());
        }
    }

    private static void playRemoveOneSound(final Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(final Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertFailSound(final Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
    }

    private static void playDropContentsSound(final Level level, final Entity entity) {
        level.playSound(
                null, entity.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F
        );
    }

    private void broadcastChangesOnContainerMenu(final Player player) {
        AbstractContainerMenu containerMenu = player.containerMenu;
        if (containerMenu != null) {
            containerMenu.slotsChanged(player.getInventory());
        }
    }
}
