package com.lum.pocket_jukebox.world.item.component;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ContainerComponent;
import net.minecraft.world.item.component.GrowableMutableContainer;
import net.minecraft.world.item.slot.SlotSelector;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PocketJukeboxContents implements ContainerComponent<PocketJukeboxContents>, TooltipComponent {
    public static final PocketJukeboxContents EMPTY = new PocketJukeboxContents(List.of());
    public static final Codec<PocketJukeboxContents> CODEC = ItemStackTemplate.CODEC.listOf().xmap(PocketJukeboxContents::new, contents -> contents.items);
    public static final StreamCodec<RegistryFriendlyByteBuf, PocketJukeboxContents> STREAM_CODEC = ItemStackTemplate.STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(PocketJukeboxContents::new, contents -> contents.items);
    private static final Fraction POCKET_JUKEBOX_IN_POCKET_JUKEBOX_WEIGHT = Fraction.getFraction(1, 16);
    private static final int NO_STACK_INDEX = -1;
    public static final int NO_SELECTED_ITEM_INDEX = -1;
    public static final DataResult<Fraction> BEEHIVE_WEIGHT = DataResult.success(Fraction.ONE);
    private final List<ItemStackTemplate> items;
    private final int selectedItem;
    private final Supplier<DataResult<Fraction>> weight;
    public static final int MAX_DISCS = 20;
    private static final Fraction DISC_WEIGHT = Fraction.getFraction(1, MAX_DISCS);

    private PocketJukeboxContents(final List<ItemStackTemplate> items, final int selectedItem) {
        this.items = items;
        this.selectedItem = selectedItem;
        this.weight = Suppliers.memoize(() -> computeContentWeight(this.items));
    }

    public PocketJukeboxContents(final List<ItemStackTemplate> items) {
        this(items, -1);
    }

    private static DataResult<Fraction> computeContentWeight(final List<? extends ItemInstance> items) {
        try {
            Fraction weight = Fraction.ZERO;

            for (ItemInstance stack : items) {
                DataResult<Fraction> itemWeight = getWeight(stack);
                if (itemWeight.isError()) {
                    return itemWeight;
                }

                weight = weight.add(itemWeight.getOrThrow().multiplyBy(Fraction.getFraction(stack.count(), 1)));
            }

            return DataResult.success(weight);
        } catch (ArithmeticException exception) {
            return DataResult.error(() -> "Excessive total pocket jukebox weight");
        }
    }

    private static DataResult<Fraction> getWeight(final ItemInstance item) {
        return DataResult.success(DISC_WEIGHT);
    }

    public static boolean canItemBeInPocketJukebox(final ItemStack itemToAdd) {
        return !itemToAdd.isEmpty() && itemToAdd.has(DataComponents.JUKEBOX_PLAYABLE);
    }

    public int getNumberOfItemsToShow() {
        return this.size();
    }

    @Override
    public Stream<ItemStack> itemCopies() {
        return this.items.stream().map(ItemStackTemplate::create);
    }

    public List<ItemStackTemplate> items() {
        return this.items;
    }

    @Override
    public int size() {
        return this.items.size();
    }

    public DataResult<Fraction> weight() {
        return this.weight.get();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public int getSelectedItemIndex() {
        return this.selectedItem;
    }

    public @Nullable ItemStackTemplate getSelectedItem() {
        return this.selectedItem == -1 ? null : this.items.get(this.selectedItem);
    }

    public PocketJukeboxContents copyWithContents(final Stream<ItemStack> newContents) {
        PocketJukeboxContents.Mutable builder = new PocketJukeboxContents.Mutable();
        newContents.forEach(builder::tryInsert);
        return builder.toImmutable();
    }

    public PocketJukeboxContents.Mutable asMutable() {
        DataResult<Fraction> currentWeight = this.weight.get();
        if (currentWeight.isError()) {
            return new PocketJukeboxContents.Mutable();
        }

        List<ItemStack> itemsList = new ArrayList<>(this.items.size());

        for (ItemStackTemplate item : this.items) {
            itemsList.add(item.create());
        }

        return new PocketJukeboxContents.Mutable(itemsList, currentWeight.getOrThrow(), this.selectedItem);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else {
            return obj instanceof PocketJukeboxContents contents ? this.items.equals(contents.items) : false;
        }
    }

    @Override
    public int hashCode() {
        return this.items.hashCode();
    }

    @Override
    public String toString() {
        return "PocketJukeboxContents" + this.items;
    }

    public static class Mutable extends GrowableMutableContainer<PocketJukeboxContents> {
        private Fraction weight;
        private int selectedItem;
        private boolean needsFlattening;

        private Mutable(final List<ItemStack> items, final Fraction weight, final int selectedItem) {
            super(items);
            this.weight = weight;
            this.selectedItem = selectedItem;
        }

        public Mutable() {
            this(new ArrayList<>(), Fraction.ZERO, -1);
        }

        public PocketJukeboxContents.Mutable clearItems() {
            this.items.clear();
            this.weight = Fraction.ZERO;
            this.selectedItem = -1;
            return this;
        }

        private int findStackIndexWithinRange(final ItemStack itemsToAdd, final int minInclusive, final int maxExclusive) {
            if (!itemsToAdd.isStackable()) {
                return -1;
            }

            int startIndex = Math.max(minInclusive, 0);
            int endIndex = Math.min(maxExclusive, this.items.size());

            for (int i = startIndex; i < endIndex; i++) {
                if (ItemStack.isSameItemSameComponents(this.items.get(i), itemsToAdd)) {
                    return i;
                }
            }

            return -1;
        }

        private int findStackIndex(final ItemStack itemsToAdd) {
            return this.findStackIndexWithinRange(itemsToAdd, 0, this.items.size());
        }

        private int getMaxAmountToAdd(final Fraction itemWeight) {
            Fraction remainingWeight = Fraction.ONE.subtract(this.weight);
            return Math.max(remainingWeight.divideBy(itemWeight).intValue(), 0);
        }

        public int tryInsert(final ItemStack itemsToAdd) {
            if (!PocketJukeboxContents.canItemBeInPocketJukebox(itemsToAdd)) {
                return 0;
            }

            DataResult<Fraction> maybeItemWeight = PocketJukeboxContents.getWeight(itemsToAdd);
            if (maybeItemWeight.isError()) {
                return 0;
            }

            Fraction itemWeight = maybeItemWeight.getOrThrow();
            int amountToAdd = Math.min(itemsToAdd.getCount(), this.getMaxAmountToAdd(itemWeight));
            if (amountToAdd == 0) {
                return 0;
            }

            this.weight = this.weight.add(getStackedWeight(itemWeight, amountToAdd));
            int stackIndex = this.findStackIndex(itemsToAdd);
            if (stackIndex != -1) {
                ItemStack removedStack = this.items.remove(stackIndex);
                ItemStack mergedStack = removedStack.copyWithCount(removedStack.getCount() + amountToAdd);
                itemsToAdd.shrink(amountToAdd);
                this.items.addFirst(mergedStack);
            } else {
                this.items.addFirst(itemsToAdd.split(amountToAdd));
            }

            return amountToAdd;
        }

        public int tryTransfer(final Slot slot, final Player player) {
            ItemStack other = slot.getItem();
            DataResult<Fraction> itemWeight = PocketJukeboxContents.getWeight(other);
            if (itemWeight.isError()) {
                return 0;
            }

            int maxAmount = this.getMaxAmountToAdd(itemWeight.getOrThrow());
            return PocketJukeboxContents.canItemBeInPocketJukebox(other) ? this.tryInsert(slot.safeTake(other.getCount(), maxAmount, player)) : 0;
        }

        public void toggleSelectedItem(final int selectedItem) {
            this.selectedItem = this.selectedItem != selectedItem && !this.indexIsOutsideAllowedBounds(selectedItem) ? selectedItem : -1;
        }

        private boolean indexIsOutsideAllowedBounds(final int selectedItem) {
            return selectedItem < 0 || selectedItem >= this.items.size();
        }

        public @Nullable ItemStack removeOne() {
            if (this.items.isEmpty()) {
                return null;
            }

            int removeIndex = this.indexIsOutsideAllowedBounds(this.selectedItem) ? 0 : this.selectedItem;
            ItemStack stack = this.items.remove(removeIndex).copy();
            this.weight = this.weight.subtract(getStackedWeight(stack));
            this.toggleSelectedItem(-1);
            return stack;
        }

        private static Fraction getStackedWeight(final Fraction weight, final int count) {
            return weight.multiplyBy(Fraction.getFraction(count, 1));
        }

        private static Fraction getStackedWeight(final ItemStack stack) {
            return getStackedWeight(PocketJukeboxContents.getWeight(stack).getOrThrow(), stack.getCount());
        }

        public Fraction weight() {
            return this.weight;
        }

        @Override
        public int replaceSlotItems(final ItemProvider newItems, final SlotSelector slotSelector) {
            this.mergeIdenticalStacks();
            return super.replaceSlotItems(newItems, slotSelector);
        }

        @Override
        public void modifySlots(final Consumer<? super SlotAccess> consumer, final SlotSelector slotSelector) {
            this.mergeIdenticalStacks();
            super.modifySlots(consumer, slotSelector);
        }

        @Override
        protected boolean setItem(final int slot, final ItemStack itemStack) {
            ItemStack currentItem = this.items.get(slot);
            Fraction adjustedWeight = currentItem.isEmpty() ? this.weight : this.weight.subtract(getStackedWeight(currentItem));
            Fraction newWeight = itemStack.isEmpty() ? adjustedWeight : getWeightWithAddedItems(adjustedWeight, itemStack);
            if (newWeight != null && super.setItem(slot, itemStack)) {
                this.weight = newWeight;
                this.needsFlattening = true;
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected boolean addSlotWithItem(final ItemProvider newItems) {
            if (!newItems.findNextNonEmpty()) {
                return false;
            } else {
                Fraction newWeight = getWeightWithAddedItems(this.weight, newItems.peek());
                if (newWeight != null && super.addSlotWithItem(newItems)) {
                    this.weight = newWeight;
                    this.needsFlattening = true;
                    return true;
                } else {
                    return false;
                }
            }
        }

        private static @Nullable Fraction getWeightWithAddedItems(final Fraction weight, final ItemStack itemsToAdd) {
            if (PocketJukeboxContents.canItemBeInPocketJukebox(itemsToAdd)) {
                DataResult<Fraction> itemWeight = PocketJukeboxContents.getWeight(itemsToAdd);
                if (itemWeight.isError()) {
                    return null;
                }

                Fraction newWeight = weight.add(getStackedWeight(itemWeight.getOrThrow(), itemsToAdd.getCount()));
                if (newWeight.compareTo(Fraction.ONE) <= 0) {
                    return newWeight;
                }
            }

            return null;
        }

        @Override
        public boolean canInsertNewSlots() {
            return this.weight.compareTo(Fraction.ONE) < 0;
        }

        private void mergeIdenticalStacks() {
            if (this.needsFlattening) {
                for (int index = 0; index < this.items.size(); index++) {
                    ItemStack itemStack = this.items.get(index);
                    if (itemStack.isEmpty()) {
                        this.items.remove(index--);
                    } else {
                        int stackIndex = this.findStackIndexWithinRange(itemStack, index + 1, this.items.size());
                        if (stackIndex != -1) {
                            ItemStack targetStack = this.items.get(stackIndex);
                            int mergedCount = itemStack.getCount() + targetStack.getCount();
                            this.items.set(stackIndex, itemStack.copyWithCount(mergedCount));
                            this.items.remove(index--);
                        }
                    }
                }

                this.needsFlattening = false;
            }
        }

        public PocketJukeboxContents toImmutable() {
            this.mergeIdenticalStacks();
            ImmutableList.Builder<ItemStackTemplate> builder = ImmutableList.builder();

            for (ItemStack item : this.items) {
                builder.add(ItemStackTemplate.fromNonEmptyStack(item));
            }

            return new PocketJukeboxContents(builder.build(), this.selectedItem);
        }
    }
}
