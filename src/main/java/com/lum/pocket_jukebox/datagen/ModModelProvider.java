package com.lum.pocket_jukebox.datagen;

import com.lum.pocket_jukebox.client.renderer.item.PocketJukeboxSelectedItemSpecialRenderer;
import com.lum.pocket_jukebox.client.renderer.item.properties.conditional.PocketJukeboxHasSelectedItem;
import com.lum.pocket_jukebox.world.item.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.ColorCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModModelProvider extends FabricModelProvider {
    private static final ModelTemplate OPEN_BACK_TEMPLATE = new ModelTemplate(
            Optional.of(Identifier.withDefaultNamespace("item/template_bundle_open_back")),
            Optional.empty(),
            TextureSlot.LAYER0);

    private static final ModelTemplate OPEN_FRONT_TEMPLATE = new ModelTemplate(
            Optional.of(Identifier.withDefaultNamespace("item/template_bundle_open_front")),
            Optional.empty(),
            TextureSlot.LAYER0);

    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        itemModelGenerator.generateFlatItem(ModItems.BLANK_DISC, ModelTemplates.FLAT_ITEM);

        generatePocketJukebox(itemModelGenerator, ModItems.POCKET_JUKEBOX);
        ModItems.DYED_POCKET_JUKEBOX.forEach(item -> generatePocketJukebox(itemModelGenerator, item));
    }

    private void generatePocketJukebox(ItemModelGenerators generators, Item item) {
        Identifier backId = ModelLocationUtils.getModelLocation(item, "_open_back");
        Identifier frontId = ModelLocationUtils.getModelLocation(item, "_open_front");

        // closed: item/<name>.json, texture item/<name>
        Identifier closedModel = ModelTemplates.FLAT_ITEM.create(item,
                TextureMapping.singleSlot(TextureSlot.LAYER0,
                        new Material(ModelLocationUtils.getModelLocation(item))),
                generators.modelOutput);

        // open back: item/<name>_open_back.json, texture item/<name>_open_back
        OPEN_BACK_TEMPLATE.create(backId,
                TextureMapping.singleSlot(TextureSlot.LAYER0, new Material(backId)),
                generators.modelOutput);

        // open front: item/<name>_open_front.json, texture item/<name>_open_front
        OPEN_FRONT_TEMPLATE.create(frontId,
                TextureMapping.singleSlot(TextureSlot.LAYER0, new Material(frontId)),
                generators.modelOutput);

        ItemModel.Unbaked closed = ItemModelUtils.plainModel(closedModel);

        // back first (bottom layer), then the selected disc, then front last (top layer)
        ItemModel.Unbaked openWithDisc = ItemModelUtils.composite(
                ItemModelUtils.plainModel(backId),
                new PocketJukeboxSelectedItemSpecialRenderer.Unbaked(),
                ItemModelUtils.plainModel(frontId));

        ItemModel.Unbaked guiModel = ItemModelUtils.conditional(
                new PocketJukeboxHasSelectedItem(), openWithDisc, closed);

        generators.itemModelOutput.accept(item,
                ItemModelUtils.select(new DisplayContext(), closed,
                        ItemModelUtils.when(ItemDisplayContext.GUI, guiModel)));
    }

    private static <T> List<T> toList(ColorCollection<T> collection) {
        List<T> list = new ArrayList<>();
        collection.forEach(list::add);
        return list;
    }

    @Override
    public String getName() {
        return "ModModelProvider";
    }
}
