package com.lum.pocket_jukebox.world.item;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.ColorCollection;

import java.util.List;

public class ModCreativeModeTabs {
    public static final ResourceKey<CreativeModeTab> MUSIC_FEATURES = createKey("music_features");

    private static ResourceKey<CreativeModeTab> createKey(final String id) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace(id));
    }

    private static void registerColoredItems(
            final CreativeModeTab.Output output, final List<DyeColor> gameplayColorOrder, final ColorCollection<Item> items
    ) {
        gameplayColorOrder.forEach(color -> output.accept(items.pick(color)));
    }

    public static void initialize() {
        List<DyeColor> gameplayColorOrder = List.of(
                DyeColor.WHITE,
                DyeColor.LIGHT_GRAY,
                DyeColor.GRAY,
                DyeColor.BLACK,
                DyeColor.BROWN,
                DyeColor.RED,
                DyeColor.ORANGE,
                DyeColor.YELLOW,
                DyeColor.LIME,
                DyeColor.GREEN,
                DyeColor.CYAN,
                DyeColor.LIGHT_BLUE,
                DyeColor.BLUE,
                DyeColor.PURPLE,
                DyeColor.MAGENTA,
                DyeColor.PINK
        );

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                MUSIC_FEATURES,
                FabricCreativeModeTab.builder()
                        .icon(() -> new ItemStack(ModItems.POCKET_JUKEBOX))
                        .title(Component.translatable("creativeTab.pocket_jukebox"))
                        .displayItems((params, musicFeatures) -> {
                            musicFeatures.accept(ModItems.POCKET_JUKEBOX);
                            registerColoredItems(musicFeatures, gameplayColorOrder, ModItems.DYED_POCKET_JUKEBOX);
                            musicFeatures.accept(ModItems.BLANK_DISC);
                        })
                        .build());
    }
}
