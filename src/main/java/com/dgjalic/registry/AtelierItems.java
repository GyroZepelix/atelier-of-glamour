package com.dgjalic.registry;

import com.dgjalic.AtelierOfGlamour;
import com.dgjalic.item.DyeableSpellbookItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class AtelierItems {
    public static final DyeableSpellbookItem DYEABLE_SPELLBOOK = register(
        "dyeable_spellbook",
        new DyeableSpellbookItem(new Item.Properties().stacksTo(1))
    );

    private AtelierItems() {
    }

    public static void register() {
    }

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(AtelierOfGlamour.MOD_ID, name), item);
    }
}
