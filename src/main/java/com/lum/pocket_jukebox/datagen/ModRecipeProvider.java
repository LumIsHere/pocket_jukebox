package com.lum.pocket_jukebox.datagen;

import com.lum.pocket_jukebox.PocketJukebox;
import com.lum.pocket_jukebox.world.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.TransmuteRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.ColorCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected RecipeProvider createRecipeProvider(
            HolderLookup.Provider registries,
            BootstrapContext<Recipe<?>> recipeOutput,
            BootstrapContext<Advancement> advancementOutput) {
        HolderLookup.RegistryLookup<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);

        return new RecipeProvider(recipeOutput, advancementOutput) {
            @Override
            public void buildRecipes() {
                // 1. Pocket Jukebox: empty bundle + blank disc + copper nuggets
                shaped(RecipeCategory.TOOLS, ModItems.POCKET_JUKEBOX)
                        .pattern("cmc")
                        .pattern("cbc")
                        .pattern("ccc")
                        .define('c', Items.COPPER_NUGGET)
                        .define('m', ModItems.BLANK_DISC)
                        .define('b', emptyBundle(Items.BUNDLE))
                        .unlockedBy("has_blank_disc", has(ModItems.BLANK_DISC))
                        .save(output);

                // 2. Dyed Pocket Jukeboxes: the bundle of the same color
                List<Item> bundles = toList(Items.DYED_BUNDLE);
                List<Item> jukeboxes = toList(ModItems.DYED_POCKET_JUKEBOX);

                for (int i = 0; i < jukeboxes.size(); i++) {
                    shaped(RecipeCategory.TOOLS, jukeboxes.get(i))
                            .pattern("cmc")
                            .pattern("cbc")
                            .pattern("ccc")
                            .define('c', Items.COPPER_NUGGET)
                            .define('m', ModItems.BLANK_DISC)
                            .define('b', emptyBundle(bundles.get(i)))
                            .unlockedBy("has_blank_disc", has(ModItems.BLANK_DISC))
                            .save(output);
                }

                shaped(RecipeCategory.MISC, ModItems.BLANK_DISC)
                        .pattern(" g ")
                        .pattern("gpg")
                        .pattern(" g ")
                        .define('g', Items.GLASS_PANE)
                        .define('p', Items.PAPER)
                        .unlockedBy("has_glass_pane", has(Items.GLASS_PANE))
                        .unlockedBy("has_paper", has(Items.PAPER))
                        .save(output);

                // 4. Dyeing: any pocket jukebox (from the tag) + a dye, contents are kept
                Ingredient anyJukebox = Ingredient.of(itemLookup.getOrThrow(ModItemTagProvider.POCKET_JUKEBOXES));
                List<Item> dyes = toList(Items.DYE);

                for (int i = 0; i < jukeboxes.size(); i++) {
                    Item result = jukeboxes.get(i);
                    String name = BuiltInRegistries.ITEM.getKey(result).getPath() + "_dye";

                    TransmuteRecipeBuilder.transmute(RecipeCategory.TOOLS, anyJukebox, Ingredient.of(dyes.get(i)), result)
                            .group("pocket_jukebox_dye")
                            .unlockedBy("has_pocket_jukebox", has(ModItems.POCKET_JUKEBOX))
                            .save(output, PocketJukebox.id(name).toString());
                }
            }
        };
    }

    private static Ingredient emptyBundle(final Item bundle) {
        return DefaultCustomIngredients.components(
                Ingredient.of(bundle),
                DataComponentPatch.builder()
                        .set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)
                        .build());
    }

    private static <T> List<T> toList(final ColorCollection<? extends T> collection) {
        List<T> list = new ArrayList<>();
        collection.forEach(list::add);
        return list;
    }

    @Override
    public String getName() {
        return "ModRecipeProvider";
    }
}