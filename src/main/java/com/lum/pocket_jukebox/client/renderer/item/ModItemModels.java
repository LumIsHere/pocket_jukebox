package com.lum.pocket_jukebox.client.renderer.item;

import com.lum.pocket_jukebox.PocketJukebox;
import com.lum.pocket_jukebox.client.renderer.item.properties.conditional.PocketJukeboxHasSelectedItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;

@Environment(EnvType.CLIENT)
public final class ModItemModels {
    private static boolean registered;

    public static void bootstrap() {
        if (registered) {
            return;
        }
        registered = true;

        ItemModels.ID_MAPPER.put(
                PocketJukebox.id("pocket_jukebox/selected_item"),
                PocketJukeboxSelectedItemSpecialRenderer.Unbaked.MAP_CODEC);

        ConditionalItemModelProperties.ID_MAPPER.put(
                PocketJukebox.id("pocket_jukebox/has_selected_item"),
                PocketJukeboxHasSelectedItem.MAP_CODEC);
    }
}