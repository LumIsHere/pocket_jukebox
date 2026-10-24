package com.lum.pocket_jukebox.client.gui;

import com.lum.pocket_jukebox.network.protocol.game.ServerboundSelectPocketJukeboxItemPacket;
import com.lum.pocket_jukebox.tags.ModItemTags;
import com.lum.pocket_jukebox.world.item.PocketJukeboxItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

@Environment(EnvType.CLIENT)
public class PocketJukeboxMouseActions implements ItemSlotMouseAction {
    private final Minecraft minecraft;
    private final ScrollWheelHandler scrollWheelHandler;

    public PocketJukeboxMouseActions(final Minecraft minecraft) {
        this.minecraft = minecraft;
        this.scrollWheelHandler = new ScrollWheelHandler();
    }

    @Override
    public boolean matches(final Slot slot) {
        return slot.getItem().is(ModItemTags.POCKET_JUKEBOXES);
    }

    @Override
    public boolean onMouseScrolled(final double scrollX, final double scrollY, final int slotIndex, final ItemStack itemStack) {
        int amountOfShownItems = PocketJukeboxItem.getNumberOfItemsToShow(itemStack);
        if (amountOfShownItems == 0) {
            return false;
        }

        Vector2i wheelXY = this.scrollWheelHandler.onMouseScroll(scrollX, scrollY);
        int wheel = wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
        if (wheel != 0) {
            int selectedItem = PocketJukeboxItem.getSelectedItemIndex(itemStack);
            int updatedSelectedItem = ScrollWheelHandler.getNextScrollWheelSelection(wheel, selectedItem, amountOfShownItems);
            if (selectedItem != updatedSelectedItem) {
                this.toggleSelectedPocketJukeboxItem(itemStack, slotIndex, updatedSelectedItem);
            }
        }

        return true;
    }

    @Override
    public void onStopHovering(final Slot hoveredSlot) {
        this.unselectedPocketJukeboxItem(hoveredSlot.getItem(), hoveredSlot.index);
    }

    @Override
    public void onSlotClicked(final Slot slot, final ContainerInput containerInput) {
        if (containerInput == ContainerInput.QUICK_MOVE || containerInput == ContainerInput.SWAP) {
            this.unselectedPocketJukeboxItem(slot.getItem(), slot.index);
        }
    }

    private void toggleSelectedPocketJukeboxItem(final ItemStack stack, final int slotIndex, final int selectedItem) {
        if (this.minecraft.getConnection() != null && selectedItem < PocketJukeboxItem.getNumberOfItemsToShow(stack)) {
            PocketJukeboxItem.toggleSelectedItem(stack, selectedItem);
            ClientPlayNetworking.send(new ServerboundSelectPocketJukeboxItemPacket(slotIndex, selectedItem));
        }
    }

    public void unselectedPocketJukeboxItem(final ItemStack pocketJukeboxItem, final int slotIndex) {
        this.toggleSelectedPocketJukeboxItem(pocketJukeboxItem, slotIndex, -1);
    }
}
