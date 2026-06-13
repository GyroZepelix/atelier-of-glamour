package com.dgjalic.recipe;

import at.petrak.hexcasting.common.lib.HexItems;
import com.dgjalic.registry.AtelierItems;
import com.dgjalic.registry.AtelierRecipeSerializers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;

public class DyeableSpellbookRecipe extends CustomRecipe {
    public DyeableSpellbookRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean foundLeather = false;
        boolean foundSpellbook = false;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.is(HexItems.SPELLBOOK)) {
                if (foundSpellbook) {
                    return false;
                }
                foundSpellbook = true;
            } else if (stack.is(Items.LEATHER)) {
                if (foundLeather) {
                    return false;
                }
                foundLeather = true;
            } else {
                return false;
            }
        }

        return foundLeather && foundSpellbook;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.is(HexItems.SPELLBOOK)) {
                ItemStack output = new ItemStack(AtelierItems.DYEABLE_SPELLBOOK);
                output.setTag(stack.getTag() == null ? null : stack.getTag().copy());
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
        return AtelierRecipeSerializers.DYEABLE_SPELLBOOK;
    }
}
