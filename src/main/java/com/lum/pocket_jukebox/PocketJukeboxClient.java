package com.lum.pocket_jukebox;

import com.lum.pocket_jukebox.client.ClientboundNowPlayingPacket;
import com.lum.pocket_jukebox.client.gui.screens.inventory.tooltip.ClientPocketJukeboxTooltip;
import com.lum.pocket_jukebox.client.renderer.item.ModItemModels;
import com.lum.pocket_jukebox.inventory.tooltip.PocketJukeboxTooltip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;

public class PocketJukeboxClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTooltipComponentCallback.EVENT.register(data ->
                data instanceof PocketJukeboxTooltip(
                        com.lum.pocket_jukebox.world.item.component.PocketJukeboxContents contents
                )
                        ? new ClientPocketJukeboxTooltip(contents)
                        : null);

        ClientPlayNetworking.registerGlobalReceiver(ClientboundNowPlayingPacket.TYPE, (payload, context) ->
                context.client().gui.hud.setOverlayMessage(payload.message(), true));

        ModItemModels.bootstrap();
    }
}