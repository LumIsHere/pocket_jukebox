package com.lum.pocket_jukebox;

import com.lum.pocket_jukebox.client.ClientboundNowPlayingPacket;
import com.lum.pocket_jukebox.component.ModDataComponents;
import com.lum.pocket_jukebox.network.protocol.game.ServerboundSelectPocketJukeboxItemPacket;
import com.lum.pocket_jukebox.world.item.*;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class PocketJukebox implements ModInitializer {
	public static final String MOD_ID = "pocket_jukebox";

	@Override
	public void onInitialize() {
        ModItems.initialize();
        ModItemIds.initialize();
        ModCreativeModeTabs.initialize();
        ModDataComponents.initialize();

        PayloadTypeRegistry.serverboundPlay().register(
                ServerboundSelectPocketJukeboxItemPacket.TYPE,
                ServerboundSelectPocketJukeboxItemPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                ServerboundSelectPocketJukeboxItemPacket.TYPE,
                (payload, context) -> {
                    AbstractContainerMenu menu = context.player().containerMenu;
                    int slotId = payload.slotId();
                    if (slotId >= 0 && slotId < menu.slots.size()) {
                        ItemStack stack = menu.slots.get(slotId).getItem();
                        if (stack.getItem() instanceof PocketJukeboxItem) {
                            PocketJukeboxItem.toggleSelectedItem(stack, payload.selectedItemIndex());
                        }
                    }
                });

        ServerTickEvents.END_SERVER_TICK.register(PocketJukeboxPlayback::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> PocketJukeboxPlayback.clearAll());
        PayloadTypeRegistry.clientboundPlay().register(
                ClientboundNowPlayingPacket.TYPE, ClientboundNowPlayingPacket.CODEC);

	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
