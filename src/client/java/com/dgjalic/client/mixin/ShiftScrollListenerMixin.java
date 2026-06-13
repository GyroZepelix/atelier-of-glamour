package com.dgjalic.client.mixin;

import at.petrak.hexcasting.client.ShiftScrollListener;
import com.dgjalic.registry.AtelierItems;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ShiftScrollListener.class, remap = false)
public class ShiftScrollListenerMixin {
    @Inject(method = "IsScrollableItem", at = @At("HEAD"), cancellable = true)
    private static void atelier$isScrollableItem(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (item == AtelierItems.DYEABLE_SPELLBOOK) {
            cir.setReturnValue(true);
        }
    }
}
