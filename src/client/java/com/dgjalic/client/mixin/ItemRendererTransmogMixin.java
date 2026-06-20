package com.dgjalic.client.mixin;

import com.dgjalic.transmog.TransmogData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererTransmogMixin {
    @Shadow
    public abstract ItemModelShaper getItemModelShaper();

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void atelier$getTransmogModel(ItemStack stack, Level level, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        var appearanceStack = TransmogData.createAppearanceStack(stack);
        if (appearanceStack.isEmpty()) {
            return;
        }

        var targetStack = appearanceStack.get();
        var model = getItemModelShaper().getItemModel(targetStack);
        var clientLevel = level instanceof ClientLevel ? (ClientLevel) level : null;
        var resolvedModel = model.getOverrides().resolve(model, targetStack, clientLevel, entity, seed);
        cir.setReturnValue(resolvedModel == null ? model : resolvedModel);
    }

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("HEAD"), cancellable = true)
    private void atelier$renderTransmog(ItemStack stack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, BakedModel model, CallbackInfo ci) {
        var appearanceStack = TransmogData.createAppearanceStack(stack);
        if (appearanceStack.isEmpty()) {
            return;
        }

        ((ItemRenderer) (Object) this).render(appearanceStack.get(), displayContext, leftHand, poseStack, buffer, light, overlay, model);
        ci.cancel();
    }
}
