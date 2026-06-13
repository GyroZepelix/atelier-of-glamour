package com.dgjalic.registry;

import com.dgjalic.AtelierOfGlamour;
import com.dgjalic.recipe.DyeableSealSpellbookRecipe;
import com.dgjalic.recipe.DyeableSpellbookRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public final class AtelierRecipeSerializers {
    public static final RecipeSerializer<DyeableSpellbookRecipe> DYEABLE_SPELLBOOK =
        new SimpleCraftingRecipeSerializer<>(DyeableSpellbookRecipe::new);
    public static final RecipeSerializer<DyeableSealSpellbookRecipe> SEAL_DYEABLE_SPELLBOOK =
        new SimpleCraftingRecipeSerializer<>(DyeableSealSpellbookRecipe::new);

    private AtelierRecipeSerializers() {
    }

    public static void register() {
        register("dyeable_spellbook", DYEABLE_SPELLBOOK);
        register("seal_dyeable_spellbook", SEAL_DYEABLE_SPELLBOOK);
    }

    private static <T extends RecipeSerializer<?>> T register(String name, T serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, new ResourceLocation(AtelierOfGlamour.MOD_ID, name), serializer);
    }
}
