package com.dgjalic.registry;

import at.petrak.hexcasting.common.lib.HexItems;
import com.dgjalic.AtelierOfGlamour;
import com.dgjalic.item.DyeableSpellbookItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public final class AtelierItems {
    private static final ResourceKey<CreativeModeTab> HEXCASTING_TAB = ResourceKey.create(
        Registries.CREATIVE_MODE_TAB,
        new ResourceLocation("hexcasting", "hexcasting")
    );

    public static final DyeableSpellbookItem DYEABLE_SPELLBOOK = register(
        "dyeable_spellbook",
        new DyeableSpellbookItem(new Item.Properties().stacksTo(1))
    );

    private AtelierItems() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(HEXCASTING_TAB)
            .register(entries -> entries.addAfter(HexItems.SPELLBOOK, DYEABLE_SPELLBOOK));
    }

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(AtelierOfGlamour.MOD_ID, name), item);
    }
}
