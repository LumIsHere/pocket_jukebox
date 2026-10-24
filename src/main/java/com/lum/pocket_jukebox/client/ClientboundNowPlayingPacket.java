package com.lum.pocket_jukebox.client;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundNowPlayingPacket(Component message) implements CustomPacketPayload {
    public static final Type<ClientboundNowPlayingPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("music_features", "now_playing"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundNowPlayingPacket> CODEC =
            StreamCodec.composite(
                    ComponentSerialization.STREAM_CODEC, ClientboundNowPlayingPacket::message,
                    ClientboundNowPlayingPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}