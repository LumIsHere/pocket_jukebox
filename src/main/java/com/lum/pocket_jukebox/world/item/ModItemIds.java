package com.lum.pocket_jukebox.world.item;

import com.lum.pocket_jukebox.PocketJukebox;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ColorCollection;

public class ModItemIds {
    public static final ResourceKey<Item> POCKET_JUKEBOX = create("pocket_jukebox");
    public static final ColorCollection<ResourceKey<Item>> DYED_POCKET_JUKEBOX = createSimpleColored("pocket_jukebox");
    public static final ResourceKey<Item> BLANK_DISC =  create("blank_disc");

    public static ResourceKey<Item> create(String name) {
        return ResourceKey.create(Registries.ITEM, PocketJukebox.id(name));
    }
    private static ColorCollection<ResourceKey<Item>> createSimpleColored(final String baseName) {
        return ColorCollection.prefixWithColor(ColorCollection.create(baseName)).map(ModItemIds::create);
    }

    public static void initialize() {
    }
}
