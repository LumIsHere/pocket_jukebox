package com.lum.pocket_jukebox;

import com.lum.pocket_jukebox.client.renderer.item.ModItemModels;
import com.lum.pocket_jukebox.datagen.ModItemTagProvider;
import com.lum.pocket_jukebox.datagen.ModModelProvider;
import com.lum.pocket_jukebox.datagen.ModRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class PocketJukeboxDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        ModItemModels.bootstrap();

        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModModelProvider::new);
        pack.addProvider(ModRecipeProvider::new);
        pack.addProvider(ModItemTagProvider::new);
	}
}
