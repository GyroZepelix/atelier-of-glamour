package com.dgjalic.transmog;

import com.dgjalic.AtelierOfGlamour;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class TransmogData {
    private static final String ROOT_TAG = "Transmog";
    private static final String TARGET_ITEM_TAG = "TargetItem";
    private static final String APPLIED_GENERATED_NAME_TAG = "AppliedGeneratedName";
    private static final String GENERATED_NAME_TAG = "GeneratedName";

    private TransmogData() {
    }

    public static boolean hasTransmog(ItemStack stack) {
        return getTargetId(stack).isPresent();
    }

    public static Optional<ResourceLocation> getTargetId(ItemStack stack) {
        var tag = stack.getTagElement(ROOT_TAG);
        if (tag == null || !tag.contains(TARGET_ITEM_TAG, 8)) {
            return Optional.empty();
        }

        var id = tag.getString(TARGET_ITEM_TAG);
        if (!ResourceLocation.isValidResourceLocation(id)) {
            return Optional.empty();
        }
        return Optional.of(new ResourceLocation(id));
    }

    public static Optional<Item> getTargetItem(ItemStack stack) {
        return getTargetId(stack)
            .filter(BuiltInRegistries.ITEM::containsKey)
            .map(BuiltInRegistries.ITEM::get);
    }

    public static Optional<ItemStack> createAppearanceStack(ItemStack stack) {
        return getTargetItem(stack)
            .map(ItemStack::new)
            .filter(appearanceStack -> !appearanceStack.isEmpty());
    }

    public static void apply(ItemStack stack, ResourceLocation targetId, Item targetItem) {
        var existing = stack.getTagElement(ROOT_TAG);
        boolean hasUserCustomName = stack.hasCustomHoverName() && !hasAppliedGeneratedName(stack, existing);
        var transmog = new CompoundTag();
        transmog.putString(TARGET_ITEM_TAG, targetId.toString());

        if (!hasUserCustomName) {
            var generatedName = generatedTargetName(targetItem);
            transmog.putBoolean(APPLIED_GENERATED_NAME_TAG, true);
            transmog.putString(GENERATED_NAME_TAG, Component.Serializer.toJson(generatedName));
            stack.setHoverName(generatedName);
        }

        stack.addTagElement(ROOT_TAG, transmog);
    }

    public static void clear(ItemStack stack) {
        var transmog = stack.getTagElement(ROOT_TAG);
        if (hasAppliedGeneratedName(stack, transmog)) {
            stack.resetHoverName();
        }

        stack.removeTagKey(ROOT_TAG);
    }

    public static String describeTarget(Item item) {
        var key = BuiltInRegistries.ITEM.getKey(item);
        return key == null ? AtelierOfGlamour.MOD_ID + ":unknown" : key.toString();
    }

    private static Component generatedTargetName(Item item) {
        return new ItemStack(item).getHoverName().copy().withStyle(style -> style.withItalic(false));
    }

    private static boolean hasAppliedGeneratedName(ItemStack stack, CompoundTag transmog) {
        if (transmog == null || !transmog.getBoolean(APPLIED_GENERATED_NAME_TAG)) {
            return false;
        }
        if (!transmog.contains(GENERATED_NAME_TAG, 8)) {
            return true;
        }
        return Component.Serializer.toJson(stack.getHoverName()).equals(transmog.getString(GENERATED_NAME_TAG));
    }
}
