package com.dgjalic.recipe;

import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import com.dgjalic.registry.AtelierItems;
import com.dgjalic.registry.AtelierRecipeSerializers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;

public class DyeableSealSpellbookRecipe extends CustomRecipe {
    public DyeableSealSpellbookRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean foundSealMaterial = false;
        boolean foundSpellbook = false;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (isUnsealedFilledDyeableSpellbook(stack)) {
                if (foundSpellbook) {
                    return false;
                }
                foundSpellbook = true;
            } else if (stack.is(HexTags.Items.SEAL_MATERIALS)) {
                if (foundSealMaterial) {
                    return false;
                }
                foundSealMaterial = true;
            } else {
                return false;
            }
        }

        return foundSealMaterial && foundSpellbook;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (isUnsealedFilledDyeableSpellbook(stack)) {
                ItemStack output = stack.copy();
                output.setCount(1);
                ItemSpellbook.setSealed(output, true);
                return output;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(AtelierItems.DYEABLE_SPELLBOOK);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AtelierRecipeSerializers.SEAL_DYEABLE_SPELLBOOK;
    }

    private static boolean isUnsealedFilledDyeableSpellbook(ItemStack stack) {
        return stack.is(AtelierItems.DYEABLE_SPELLBOOK)
            && AtelierItems.DYEABLE_SPELLBOOK.readIotaTag(stack) != null
            && !ItemSpellbook.isSealed(stack);
    }
}
