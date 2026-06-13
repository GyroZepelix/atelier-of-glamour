package com.dgjalic.client;

import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.common.items.storage.ItemFocus;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import com.dgjalic.registry.AtelierItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class AtelierOfGlamourClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        IClientXplatAbstractions.INSTANCE.registerItemProperty(AtelierItems.DYEABLE_SPELLBOOK, ItemFocus.OVERLAY_PRED,
            (stack, level, holder, holderId) -> {
                if (AtelierItems.DYEABLE_SPELLBOOK.readIotaTag(stack) == null
                    && !NBTHelper.hasString(stack, IotaHolderItem.TAG_OVERRIDE_VISUALLY)) {
                    return 0;
                }
                return ItemSpellbook.isSealed(stack) ? 2 : 1;
            });
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> switch (tintIndex) {
            case 0 -> AtelierItems.DYEABLE_SPELLBOOK.getColor(stack);
            case 1 -> getIotaColor(stack);
            default -> 0xFFFFFF;
        }, AtelierItems.DYEABLE_SPELLBOOK);
    }

    private static int getIotaColor(ItemStack stack) {
        if (NBTHelper.hasString(stack, IotaHolderItem.TAG_OVERRIDE_VISUALLY)) {
            var override = NBTHelper.getString(stack, IotaHolderItem.TAG_OVERRIDE_VISUALLY);
            if (override != null && ResourceLocation.isValidResourceLocation(override)) {
                var key = new ResourceLocation(override);
                if (HexIotaTypes.REGISTRY.containsKey(key)) {
                    var iotaType = HexIotaTypes.REGISTRY.get(key);
                    if (iotaType != null) {
                        return iotaType.color();
                    }
                }
            }

            return 0xFF000000 | Mth.hsvToRgb(ClientTickCounter.getTotal() * 2 % 360 / 360F, 0.75F, 1F);
        }

        var tag = AtelierItems.DYEABLE_SPELLBOOK.readIotaTag(stack);
        return tag == null ? HexUtils.ERROR_COLOR : IotaType.getColor(tag);
    }
}
