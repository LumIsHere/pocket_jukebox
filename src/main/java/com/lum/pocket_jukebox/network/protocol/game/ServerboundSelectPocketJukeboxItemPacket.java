package com.lum.pocket_jukebox.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundSelectPocketJukeboxItemPacket(int slotId, int selectedItemIndex)
        implements CustomPacketPayload {

    public static final Type<ServerboundSelectPocketJukeboxItemPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("pocket_jukebox", "select_pocket_jukebox_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSelectPocketJukeboxItemPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ServerboundSelectPocketJukeboxItemPacket::slotId,
                    ByteBufCodecs.VAR_INT, ServerboundSelectPocketJukeboxItemPacket::selectedItemIndex,
                    ServerboundSelectPocketJukeboxItemPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}