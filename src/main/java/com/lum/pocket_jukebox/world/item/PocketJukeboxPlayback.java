package com.lum.pocket_jukebox.world.item;

import com.lum.pocket_jukebox.client.ClientboundNowPlayingPacket;
import com.lum.pocket_jukebox.component.ModDataComponents;
import com.lum.pocket_jukebox.world.item.component.PocketJukeboxContents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.JukeboxSong;

import java.util.*;

public final class PocketJukeboxPlayback {
    private static final int CARRIED = -1;
    private static final int NONE = -2;
    private static final Map<UUID, State> PLAYING = new HashMap<>();

    private static final class State {
        final ItemStack disc;
        final Identifier soundId;
        final long endTick;
        int slot;
        int index;

        State(ItemStack disc, Identifier soundId, long endTick, int slot, int index) {
            this.disc = disc;
            this.soundId = soundId;
            this.endTick = endTick;
            this.slot = slot;
            this.index = index;
        }
    }

    public static boolean isPlaying(final Player player) {
        return PLAYING.containsKey(player.getUUID());
    }

    public static void play(final ServerLevel level, final ServerPlayer player, final ItemStack pocketJukebox, int index) {
        stop(player, false);

        PocketJukeboxContents contents = pocketJukebox.get(ModDataComponents.POCKET_JUKEBOX_CONTENTS);
        if (contents == null || contents.isEmpty()) {
            return;
        }
        index = Math.floorMod(index, contents.size());
        ItemStack disc = contents.items().get(index).create();

        Optional<Holder<JukeboxSong>> song = JukeboxSong.fromStack(disc);
        if (song.isEmpty()) {
            return;
        }
        JukeboxSong jukeboxSong = song.get().value();
        Holder<SoundEvent> sound = jukeboxSong.soundEvent();

        level.playSeededSound(null, player, sound, SoundSource.RECORDS, 1.0F, 1.0F, level.getRandom().nextLong());
        ServerPlayNetworking.send(player, new ClientboundNowPlayingPacket(
                Component.translatable("record.nowPlaying", jukeboxSong.description())));

        if (PocketJukeboxItem.getSelectedItemIndex(pocketJukebox) != index) {
            PocketJukeboxItem.toggleSelectedItem(pocketJukebox, index);
        }
        PLAYING.put(player.getUUID(), new State(
                disc, sound.value().location(),
                level.getGameTime() + jukeboxSong.lengthInTicks(),
                slotOf(player, pocketJukebox), index));
    }

    public static void stop(final ServerPlayer player, final boolean announce) {
        State state = PLAYING.remove(player.getUUID());
        if (state == null) {
            return;
        }
        if (player.level() instanceof ServerLevel level) {
            ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(state.soundId, SoundSource.RECORDS);
            for (ServerPlayer other : level.players()) {
                other.connection.send(packet);
            }
        }
        if (announce) {
            ServerPlayNetworking.send(player, new ClientboundNowPlayingPacket(
                    Component.translatable("message.pocket_jukebox.pocket_jukebox.pause")));
        }
    }

    public static void tick(final MinecraftServer server) {
        if (PLAYING.isEmpty()) {
            return;
        }
        for (UUID id : new ArrayList<>(PLAYING.keySet())) {
            State state = PLAYING.get(id);
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (state == null) {
                continue;
            }
            if (player == null) {
                PLAYING.remove(id);
                continue;
            }
            if (!(player.level() instanceof ServerLevel level)) {
                continue;
            }
            if (level.getGameTime() >= state.endTick) {
                PLAYING.remove(id); // song ended by itself
                continue;
            }
            if (locateDisc(player, state)) {
                continue; // disc is still inside a pocket jukebox
            }

            // The disc was taken out: play the one that took its place, or stop.
            ItemStack source = stackAt(player, state.slot);
            PocketJukeboxContents contents = source.get(ModDataComponents.POCKET_JUKEBOX_CONTENTS);
            if (source.getItem() instanceof PocketJukeboxItem && contents != null && !contents.isEmpty()) {
                play(level, player, source, state.index);
            } else {
                stop(player, false);
            }
        }
    }

    private static boolean locateDisc(final ServerPlayer player, final State state) {
        if (check(player, state, state.slot) || check(player, state, CARRIED)) {
            return true;
        }
        int size = player.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            if (check(player, state, i)) {
                return true;
            }
        }
        return false;
    }

    private static boolean check(final ServerPlayer player, final State state, final int slot) {
        ItemStack stack = stackAt(player, slot);
        if (!(stack.getItem() instanceof PocketJukeboxItem)) {
            return false;
        }
        PocketJukeboxContents contents = stack.get(ModDataComponents.POCKET_JUKEBOX_CONTENTS);
        if (contents == null) {
            return false;
        }
        List<ItemStackTemplate> items = contents.items();
        for (int i = 0; i < items.size(); i++) {
            if (ItemStack.isSameItemSameComponents(items.get(i).create(), state.disc)) {
                state.slot = slot;
                state.index = i;
                return true;
            }
        }
        return false;
    }

    private static ItemStack stackAt(final Player player, final int slot) {
        if (slot == NONE) {
            return ItemStack.EMPTY;
        }
        return slot == CARRIED ? player.containerMenu.getCarried() : player.getInventory().getItem(slot);
    }

    private static int slotOf(final Player player, final ItemStack stack) {
        int size = player.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            if (player.getInventory().getItem(i) == stack) {
                return i;
            }
        }
        return NONE;
    }

    public static void clearAll() {
        PLAYING.clear();
    }
}