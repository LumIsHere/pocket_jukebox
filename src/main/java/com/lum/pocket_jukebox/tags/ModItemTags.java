package com.lum.pocket_jukebox.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> POCKET_JUKEBOXES = bind("music_features", "pocket_jukeboxes");

    private static TagKey<Item> bind(final String namespace, final String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, path));
    }
}
