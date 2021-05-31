package com.chaosbuffalo.mkchat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class PartyCommand {
    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("p").then(Commands.argument("msg", StringArgumentType.greedyString())
                .executes(PartyCommand::handleMessage));
    }

    static int handleMessage(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        String msg = StringArgumentType.getString(ctx, "msg");
        StringTextComponent msgComp = new StringTextComponent(String.format("[Party]<%s>: %s",
                ctx.getSource().asPlayer().getName().getString(), msg));
        msgComp.mergeStyle(TextFormatting.DARK_AQUA);
        ctx.getSource().asPlayer().sendMessage(msgComp, Util.DUMMY_UUID);
        ctx.getSource().getServer().getPlayerList().sendMessageToAllTeamMembers(ctx.getSource().asPlayer(), msgComp);
        return Command.SINGLE_SUCCESS;
    }
}
