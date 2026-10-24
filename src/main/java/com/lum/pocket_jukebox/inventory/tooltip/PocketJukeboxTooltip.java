package com.lum.pocket_jukebox.inventory.tooltip;

import com.lum.pocket_jukebox.world.item.component.PocketJukeboxContents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record PocketJukeboxTooltip(PocketJukeboxContents contents) implements TooltipComponent {
}
