package com.chaosbuffalo.mkchat.command;

import com.chaosbuffalo.mkchat.capabilities.Capabilities;
import com.chaosbuffalo.mkchat.capabilities.IPlayerDialogue;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CleanHistoryCommand {
    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("chat").then(Commands.literal("history")
                .then(Commands.literal("clean").executes(CleanHistoryCommand::handleMessage)));
    }

    static int handleMessage(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ctx.getSource().asPlayer().getCapability(Capabilities.PLAYER_DIALOGUE_CAPABILITY)
                .ifPresent(IPlayerDialogue::cleanHistory);
        return Command.SINGLE_SUCCESS;
    }
}
