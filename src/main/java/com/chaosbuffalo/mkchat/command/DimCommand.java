package com.chaosbuffalo.mkchat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class DimCommand {
    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("dim").then(Commands.argument("msg", StringArgumentType.greedyString())
                .executes(DimCommand::handleMessage));
    }

    static int handleMessage(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        String msg = StringArgumentType.getString(ctx, "msg");
        StringTextComponent compMessage = new StringTextComponent(String.format("<%s>: %s",
                ctx.getSource().asPlayer().getName().getFormattedText(), msg));
        compMessage.applyTextStyle(TextFormatting.GOLD);
        // emulate sendMessage but only to players in the dimension
        ctx.getSource().getServer().sendMessage(compMessage);
        ctx.getSource().getServer().getPlayerList().sendPacketToAllPlayersInDimension(
                new SChatPacket(compMessage, ChatType.CHAT), ctx.getSource().asPlayer().dimension);
        return Command.SINGLE_SUCCESS;
    }
}
