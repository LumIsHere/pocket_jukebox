package com.lum.pocket_jukebox.world.item;

import com.lum.pocket_jukebox.component.ModDataComponents;
import com.lum.pocket_jukebox.world.item.component.PocketJukeboxContents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ColorCollection;

import java.util.function.Function;

public class ModItems {

    public static final Item POCKET_JUKEBOX = registerItem(ModItemIds.POCKET_JUKEBOX, PocketJukeboxItem::new, new Item.Properties().stacksTo(1).component(ModDataComponents.POCKET_JUKEBOX_CONTENTS, PocketJukeboxContents.EMPTY));
    public static final ColorCollection<Item> DYED_POCKET_JUKEBOX = ColorCollection.registerItems
            (ModItemIds.DYED_POCKET_JUKEBOX,
            (name, var1) -> registerItem(name, PocketJukeboxItem::new, new Item.Properties().stacksTo(1).component(ModDataComponents.POCKET_JUKEBOX_CONTENTS, PocketJukeboxContents.EMPTY)));
    public static final Item BLANK_DISC = registerItem(ModItemIds.BLANK_DISC, Item::new, new Item.Properties());

    public static Item registerItem(ResourceKey<Item> itemKey, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        Item item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }

    public static void initialize() {
    }
}
