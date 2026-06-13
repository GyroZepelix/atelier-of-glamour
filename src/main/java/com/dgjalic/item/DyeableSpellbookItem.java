package com.dgjalic.item;

import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

public class DyeableSpellbookItem extends ItemSpellbook implements DyeableLeatherItem {
    private static final int DEFAULT_COLOR = 0xFFFFFF;

    public DyeableSpellbookItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getColor(ItemStack stack) {
        var display = stack.getTagElement(TAG_DISPLAY);
        return display != null && display.contains(TAG_COLOR, 99) ? display.getInt(TAG_COLOR) : DEFAULT_COLOR;
    }

    @Override
    public int numVariants() {
        return 1;
    }
}
