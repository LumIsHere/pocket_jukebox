package com.lum.pocket_jukebox.client.gui.screens.inventory.tooltip;

import com.lum.pocket_jukebox.world.item.component.PocketJukeboxContents;
import com.mojang.serialization.DataResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientPocketJukeboxTooltip implements ClientTooltipComponent {
    private static final Identifier PROGRESSBAR_BORDER_SPRITE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_border");
    private static final Identifier PROGRESSBAR_FILL_SPRITE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_fill");
    private static final Identifier PROGRESSBAR_FULL_SPRITE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_full");
    private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_front");
    private static final Identifier SLOT_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_background");
    private static final int SLOT_SIZE = 24;
    private static final int GRID_COLUMNS = 5;
    private static final int GRID_WIDTH = GRID_COLUMNS * SLOT_SIZE;
    private static final Component POCKET_JUKEBOX_FULL_TEXT = Component.translatable("item.music_features.pocket_jukebox.full");
    private static final Component POCKET_JUKEBOX_EMPTY_TEXT = Component.translatable("item.music_features.pocket_jukebox.empty");
    private static final Component POCKET_JUKEBOX_EMPTY_DESCRIPTION = Component.translatable("item.music_features.pocket_jukebox.empty.description");
    private final PocketJukeboxContents contents;

    public ClientPocketJukeboxTooltip(final PocketJukeboxContents contents) {
        this.contents = contents;
    }

    @Override
    public int getHeight(final Font font) {
        return this.contents.isEmpty() ? getEmptyPocketJukeboxBackgroundHeight(font) : this.backgroundHeight();
    }

    @Override
    public int getWidth(final Font font) {
        return GRID_WIDTH;
    }

    @Override
    public boolean showTooltipWithItemInHand() {
        return true;
    }

    private static int getEmptyPocketJukeboxBackgroundHeight(final Font font) {
        return getEmptyPocketJukeboxDescriptionTextHeight(font) + 13 + 8;
    }

    private int backgroundHeight() {
        return this.itemGridHeight() + 13 + 8;
    }

    private int itemGridHeight() {
        return this.gridSizeY() * 24;
    }

    private static int getContentXOffset(final int tooltipWidth) {
        return (tooltipWidth - 96) / 2;
    }

    private static int getGridXOffset(final int tooltipWidth) {
        return (tooltipWidth - GRID_WIDTH) / 2;
    }

    private int gridSizeY() {
        return Mth.positiveCeilDiv(this.slotCount(), GRID_COLUMNS);
    }

    private int slotCount() {
        return Math.min(PocketJukeboxContents.MAX_DISCS, this.contents.size());
    }

    @Override
    public void extractImage(final Font font, final int x, final int y, final int w, final int h, final GuiGraphicsExtractor graphics) {
        DataResult<Fraction> weight = this.contents.weight();
        if (!weight.isError()) {
            if (this.contents.isEmpty()) {
                extractEmptyPocketJukeboxTooltip(font, x, y, w, h, graphics);
            } else {
                this.extractPocketJukeboxWithItemsTooltip(font, x, y, w, h, graphics, weight.getOrThrow());
            }
        }
    }

    private static void extractEmptyPocketJukeboxTooltip(final Font font, final int x, final int y, final int w, final int h, final GuiGraphicsExtractor graphics) {
        int left = x + getContentXOffset(w);
        extractEmptyPocketJukeboxDescriptionText(left, y, font, graphics);
        extractProgressbar(left, y + getEmptyPocketJukeboxDescriptionTextHeight(font) + 4, font, graphics, Fraction.ZERO);
    }

    private void extractPocketJukeboxWithItemsTooltip(
            final Font font, final int x, final int y, final int w, final int h, final GuiGraphicsExtractor graphics, final Fraction weight
    ) {
        List<ItemStackTemplate> shownItems = this.getShownItems(this.contents.getNumberOfItemsToShow());
        int xStartPos = x + getGridXOffset(w) + GRID_WIDTH;
        int yStartPos = y + this.gridSizeY() * SLOT_SIZE;
        int slotNumber = 1;

        for (int rowNumber = 1; rowNumber <= this.gridSizeY(); rowNumber++) {
            for (int columnNumber = 1; columnNumber <= GRID_COLUMNS; columnNumber++) {
                int drawX = xStartPos - columnNumber * SLOT_SIZE;
                int drawY = yStartPos - rowNumber * SLOT_SIZE;
                if (shouldRenderItemSlot(shownItems, slotNumber)) {
                    this.extractSlot(slotNumber, drawX, drawY, shownItems, slotNumber, font, graphics);
                    slotNumber++;
                }
            }
        }

        this.extractSelectedItemTooltip(font, graphics, x, y, w);
        extractProgressbar(x + getContentXOffset(w), y + this.itemGridHeight() + 4, font, graphics, weight);
    }

    private List<ItemStackTemplate> getShownItems(final int amountOfItemsToShow) {
        int lastToDisplay = Math.min(this.contents.size(), amountOfItemsToShow);
        return this.contents.items().subList(0, lastToDisplay);
    }

    private static boolean shouldRenderSurplusText(final boolean isOverflowing, final int column, final int row) {
        return isOverflowing && column * row == 1;
    }

    private static boolean shouldRenderItemSlot(final List<? extends ItemInstance> shownItems, final int slotNumber) {
        return shownItems.size() >= slotNumber;
    }

    private int getAmountOfHiddenItems(final List<ItemStackTemplate> shownItems) {
        return this.contents.items().stream().skip(shownItems.size()).mapToInt(ItemInstance::count).sum();
    }

    private void extractSlot(
            final int slotNumber,
            final int drawX,
            final int drawY,
            final List<ItemStackTemplate> shownItems,
            final int slotIndex,
            final Font font,
            final GuiGraphicsExtractor graphics
    ) {
        int itemVisualOrderIndex = shownItems.size() - slotNumber;
        boolean hasHighlight = itemVisualOrderIndex == this.contents.getSelectedItemIndex();
        ItemStack item = shownItems.get(itemVisualOrderIndex).create();
        if (hasHighlight) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, drawX, drawY, 24, 24);
        } else {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_BACKGROUND_SPRITE, drawX, drawY, 24, 24);
        }

        graphics.item(item, drawX + 4, drawY + 4, slotIndex);
        graphics.itemDecorations(font, item, drawX + 4, drawY + 4);
        if (hasHighlight) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, drawX, drawY, 24, 24);
        }
    }

    private static void extractCount(final int drawX, final int drawY, final int hiddenItemCount, final Font font, final GuiGraphicsExtractor graphics) {
        graphics.centeredText(font, "+" + hiddenItemCount, drawX + 12, drawY + 10, -1);
    }

    private void extractSelectedItemTooltip(final Font font, final GuiGraphicsExtractor graphics, final int x, final int y, final int w) {
        ItemStackTemplate selectedItem = this.contents.getSelectedItem();
        if (selectedItem != null) {
            ItemStack itemStack = selectedItem.create();
            Component selectedItemName = itemStack.getStyledHoverName();
            int textWidth = font.width(selectedItemName.getVisualOrderText());
            int centerTooltip = x + w / 2 - 12;
            ClientTooltipComponent selectedItemNameTooltip = ClientTooltipComponent.create(selectedItemName.getVisualOrderText());
            graphics.tooltip(
                    font,
                    List.of(selectedItemNameTooltip),
                    centerTooltip - textWidth / 2,
                    y - 15,
                    DefaultTooltipPositioner.INSTANCE,
                    itemStack.get(DataComponents.TOOLTIP_STYLE),
                    false
            );
        }
    }

    private static void extractProgressbar(final int x, final int y, final Font font, final GuiGraphicsExtractor graphics, final Fraction weight) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getProgressBarTexture(weight), x + 1, y, getProgressBarFill(weight), 13);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESSBAR_BORDER_SPRITE, x, y, 96, 13);
        Component progressBarFillText = getProgressBarFillText(weight);
        if (progressBarFillText != null) {
            graphics.centeredText(font, progressBarFillText, x + 48, y + 3, -1);
        }
    }

    private static void extractEmptyPocketJukeboxDescriptionText(final int x, final int y, final Font font, final GuiGraphicsExtractor graphics) {
        graphics.textWithWordWrap(font, POCKET_JUKEBOX_EMPTY_DESCRIPTION, x, y, 96, -5592406);
    }

    private static int getEmptyPocketJukeboxDescriptionTextHeight(final Font font) {
        return font.split(POCKET_JUKEBOX_EMPTY_DESCRIPTION, 96).size() * 9;
    }

    private static int getProgressBarFill(final Fraction weight) {
        return Mth.clamp(Mth.mulAndTruncate(weight, 94), 0, 94);
    }

    private static Identifier getProgressBarTexture(final Fraction weight) {
        return weight.compareTo(Fraction.ONE) >= 0 ? PROGRESSBAR_FULL_SPRITE : PROGRESSBAR_FILL_SPRITE;
    }

    private static @Nullable Component getProgressBarFillText(final Fraction weight) {
        if (weight.compareTo(Fraction.ZERO) == 0) {
            return POCKET_JUKEBOX_EMPTY_TEXT;
        } else {
            return weight.compareTo(Fraction.ONE) >= 0 ? POCKET_JUKEBOX_FULL_TEXT : null;
        }
    }
}
