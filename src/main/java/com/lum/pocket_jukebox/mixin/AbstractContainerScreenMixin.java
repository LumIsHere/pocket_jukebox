package com.lum.pocket_jukebox.mixin;

import com.lum.pocket_jukebox.client.gui.PocketJukeboxMouseActions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow
    protected abstract void addItemSlotMouseAction(ItemSlotMouseAction action);

    @Inject(method = "init", at = @At("TAIL"))
    private void music_features$addPocketJukeboxActions(CallbackInfo ci) {
        this.addItemSlotMouseAction(new PocketJukeboxMouseActions(Minecraft.getInstance()));
    }
}