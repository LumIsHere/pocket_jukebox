package com.lum.pocket_jukebox.client.renderer.item.properties.conditional;

import com.lum.pocket_jukebox.world.item.PocketJukeboxItem;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class PocketJukeboxHasSelectedItem implements ConditionalItemModelProperty {
    public static final MapCodec<PocketJukeboxHasSelectedItem> MAP_CODEC = MapCodec.unit(new PocketJukeboxHasSelectedItem());

    @Override
    public boolean get(
            final ItemStack itemStack, final @Nullable ClientLevel level, final @Nullable LivingEntity owner, final int seed, final ItemDisplayContext displayContext
    ) {
        return PocketJukeboxItem.getSelectedItem(itemStack) != null;
    }

    @Override
    public MapCodec<PocketJukeboxHasSelectedItem> type() {
        return MAP_CODEC;
    }
}
