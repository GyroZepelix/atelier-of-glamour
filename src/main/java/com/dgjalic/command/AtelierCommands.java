package com.dgjalic.command;

import com.dgjalic.transmog.TransmogData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public final class AtelierCommands {
    private static final SimpleCommandExceptionType EMPTY_HAND = new SimpleCommandExceptionType(
        Component.literal("Hold an item in your main hand first.")
    );
    private static final SimpleCommandExceptionType NOT_TRANSMOGRIFIED = new SimpleCommandExceptionType(
        Component.literal("The held item is not transmogrified.")
    );
    private static final DynamicCommandExceptionType UNKNOWN_ITEM = new DynamicCommandExceptionType(
        id -> Component.literal("Unknown item: " + id)
    );

    private AtelierCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
            Commands.literal("aog")
                .then(Commands.literal("transmog")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.literal("set")
                        .then(Commands.argument("item_id", ResourceLocationArgument.id())
                            .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ITEM.keySet(), builder))
                            .executes(AtelierCommands::setTransmog)))
                    .then(Commands.literal("clear")
                        .executes(AtelierCommands::clearTransmog)))
        ));
    }

    private static int setTransmog(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayerOrException();
        var stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            throw EMPTY_HAND.create();
        }

        var targetId = ResourceLocationArgument.getId(context, "item_id");
        if (!BuiltInRegistries.ITEM.containsKey(targetId)) {
            throw UNKNOWN_ITEM.create(targetId.toString());
        }

        Item targetItem = BuiltInRegistries.ITEM.get(targetId);

        TransmogData.apply(stack, targetId, targetItem);
        syncInventory(player);
        source.sendSuccess(() -> Component.literal("Transmogrified held item as " + TransmogData.describeTarget(targetItem) + "."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int clearTransmog(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayerOrException();
        var stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            throw EMPTY_HAND.create();
        }
        if (!TransmogData.hasTransmog(stack)) {
            throw NOT_TRANSMOGRIFIED.create();
        }

        TransmogData.clear(stack);
        syncInventory(player);
        source.sendSuccess(() -> Component.literal("Cleared transmog from held item."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static void syncInventory(ServerPlayer player) {
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }
}
