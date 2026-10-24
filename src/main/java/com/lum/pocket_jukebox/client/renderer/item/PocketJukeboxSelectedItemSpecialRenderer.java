package com.lum.pocket_jukebox.client.renderer.item;

import com.lum.pocket_jukebox.world.item.PocketJukeboxItem;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class PocketJukeboxSelectedItemSpecialRenderer implements ItemModel {
    private static final ItemModel INSTANCE = new PocketJukeboxSelectedItemSpecialRenderer();

    @Override
    public void update(
            final ItemStackRenderState output,
            final ItemStack item,
            final ItemModelResolver resolver,
            final ItemDisplayContext displayContext,
            final @Nullable ClientLevel level,
            final @Nullable ItemOwner owner,
            final int seed
    ) {
        output.appendModelIdentityElement(this);
        ItemStackTemplate selectedItem = PocketJukeboxItem.getSelectedItem(item);
        if (selectedItem != null) {
            resolver.appendItemLayers(output, selectedItem.create(), displayContext, level, owner, seed);
        }
    }

    @Environment(EnvType.CLIENT)
    public record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<PocketJukeboxSelectedItemSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new PocketJukeboxSelectedItemSpecialRenderer.Unbaked());

        @Override
        public MapCodec<PocketJukeboxSelectedItemSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(final ItemModel.BakingContext context, final Matrix4fc transformation) {
            return PocketJukeboxSelectedItemSpecialRenderer.INSTANCE;
        }

        @Override
        public void resolveDependencies(final ResolvableModel.Resolver resolver) {
        }
    }
}
