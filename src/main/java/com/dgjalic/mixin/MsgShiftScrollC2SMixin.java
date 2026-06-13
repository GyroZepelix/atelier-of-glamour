package com.dgjalic.mixin;

import at.petrak.hexcasting.common.msgs.MsgShiftScrollC2S;
import com.dgjalic.registry.AtelierItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MsgShiftScrollC2S.class, remap = false)
public abstract class MsgShiftScrollC2SMixin {
    @Inject(method = "handleForHand", at = @At("HEAD"), cancellable = true)
    private void atelier$handleDyeableSpellbook(ServerPlayer sender, InteractionHand hand, double delta, CallbackInfo ci) {
        if (delta == 0) {
            return;
        }

        ItemStack stack = sender.getItemInHand(hand);
        if (stack.is(AtelierItems.DYEABLE_SPELLBOOK)) {
            atelier$spellbook(sender, hand, stack, delta);
            ci.cancel();
        }
    }

    @Invoker("spellbook")
    protected abstract void atelier$spellbook(ServerPlayer sender, InteractionHand hand, ItemStack stack, double delta);
}
