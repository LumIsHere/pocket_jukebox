package com.lum.pocket_jukebox.datagen;

import com.lum.pocket_jukebox.PocketJukebox;
import com.lum.pocket_jukebox.world.item.ModItemIds;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ColorCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
    public ModItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    public static final TagKey<Item> POCKET_JUKEBOXES = TagKey.create(Registries.ITEM, PocketJukebox.id("pocket_jukeboxes"));

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        builder(POCKET_JUKEBOXES).add(ModItemIds.POCKET_JUKEBOX);

        List<ResourceKey<Item>> ids = toList(ModItemIds.DYED_POCKET_JUKEBOX);
        for (int i = 0; i < ids.size(); i++) {
            builder(POCKET_JUKEBOXES)
                    .add(ids.get(i))
                    .setReplace(true);
        }
    }

    private static <T> List<T> toList(ColorCollection<T> collection) {
        List<T> list = new ArrayList<>();
        collection.forEach(list::add);
        return list;
    }
}