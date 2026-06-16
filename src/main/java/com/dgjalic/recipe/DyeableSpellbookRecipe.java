package com.dgjalic.recipe;

import at.petrak.hexcasting.common.lib.HexItems;
import com.dgjalic.registry.AtelierItems;
import com.dgjalic.registry.AtelierRecipeSerializers;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class DyeableSpellbookRecipe extends CustomRecipe {
    private static final Map<DyeColor, TagKey<Item>> DYE_COLOR_TAGS = createDyeColorTags();

    public DyeableSpellbookRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean foundDye = false;
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
            } else if (getDye(stack).isPresent()) {
                foundDye = true;
            } else {
                return false;
            }
        }

        return foundDye && foundSpellbook;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack spellbook = ItemStack.EMPTY;
        List<DyeItem> dyes = new ArrayList<>();

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.is(HexItems.SPELLBOOK)) {
                if (!spellbook.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                spellbook = stack;
            } else {
                Optional<DyeItem> dye = getDye(stack);
                if (dye.isPresent()) {
                    dyes.add(dye.get());
                } else if (!stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (spellbook.isEmpty() || dyes.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack output = new ItemStack(AtelierItems.DYEABLE_SPELLBOOK);
        output.setTag(spellbook.getTag() == null ? null : spellbook.getTag().copy());
        return DyeableLeatherItem.dyeArmor(output, dyes);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(HexItems.SPELLBOOK));
        ingredients.add(Ingredient.of(ConventionalItemTags.DYES));
        return ingredients;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(AtelierItems.DYEABLE_SPELLBOOK);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AtelierRecipeSerializers.DYEABLE_SPELLBOOK;
    }

    private static Optional<DyeItem> getDye(ItemStack stack) {
        if (!stack.is(ConventionalItemTags.DYES)) {
            return Optional.empty();
        }
        if (stack.getItem() instanceof DyeItem dyeItem) {
            return Optional.of(dyeItem);
        }
        for (Map.Entry<DyeColor, TagKey<Item>> entry : DYE_COLOR_TAGS.entrySet()) {
            if (stack.is(entry.getValue())) {
                return Optional.of(DyeItem.byColor(entry.getKey()));
            }
        }
        return Optional.empty();
    }

    private static Map<DyeColor, TagKey<Item>> createDyeColorTags() {
        Map<DyeColor, TagKey<Item>> tags = new EnumMap<>(DyeColor.class);
        tags.put(DyeColor.WHITE, ConventionalItemTags.WHITE_DYES);
        tags.put(DyeColor.ORANGE, ConventionalItemTags.ORANGE_DYES);
        tags.put(DyeColor.MAGENTA, ConventionalItemTags.MAGENTA_DYES);
        tags.put(DyeColor.LIGHT_BLUE, ConventionalItemTags.LIGHT_BLUE_DYES);
        tags.put(DyeColor.YELLOW, ConventionalItemTags.YELLOW_DYES);
        tags.put(DyeColor.LIME, ConventionalItemTags.LIME_DYES);
        tags.put(DyeColor.PINK, ConventionalItemTags.PINK_DYES);
        tags.put(DyeColor.GRAY, ConventionalItemTags.GRAY_DYES);
        tags.put(DyeColor.LIGHT_GRAY, ConventionalItemTags.LIGHT_GRAY_DYES);
        tags.put(DyeColor.CYAN, ConventionalItemTags.CYAN_DYES);
        tags.put(DyeColor.PURPLE, ConventionalItemTags.PURPLE_DYES);
        tags.put(DyeColor.BLUE, ConventionalItemTags.BLUE_DYES);
        tags.put(DyeColor.BROWN, ConventionalItemTags.BROWN_DYES);
        tags.put(DyeColor.GREEN, ConventionalItemTags.GREEN_DYES);
        tags.put(DyeColor.RED, ConventionalItemTags.RED_DYES);
        tags.put(DyeColor.BLACK, ConventionalItemTags.BLACK_DYES);
        return tags;
    }
}
