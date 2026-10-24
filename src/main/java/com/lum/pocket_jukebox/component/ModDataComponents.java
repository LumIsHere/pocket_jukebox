package com.lum.pocket_jukebox.component;

import com.lum.pocket_jukebox.world.item.component.PocketJukeboxContents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final DataComponentType<PocketJukeboxContents> POCKET_JUKEBOX_CONTENTS = register(
            "pocket_jukebox_contents", b -> b.persistent(PocketJukeboxContents.CODEC).networkSynchronized(PocketJukeboxContents.STREAM_CODEC).cacheEncoding()
    );
    private static <T> DataComponentType<T> register(final String id, final UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, builder.apply(DataComponentType.builder()).build());
    }
    public static void initialize() {
    }
}
