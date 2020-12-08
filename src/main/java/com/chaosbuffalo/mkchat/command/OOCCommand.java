package com.chaosbuffalo.mkchat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class OOCCommand {
    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("ooc").then(Commands.argument("msg", StringArgumentType.greedyString())
                .executes(OOCCommand::handleMessage));
    }

    static int handleMessage(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        String msg = StringArgumentType.getString(ctx, "msg");
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        StringTextComponent oocMessage = new StringTextComponent(String.format("[OOC]<%s>: %s",
                ctx.getSource().asPlayer().getName().getString(), msg));
        oocMessage.mergeStyle(TextFormatting.DARK_GREEN);
        player.getServerWorld().getPlayers().forEach(
                playerEntity -> playerEntity.sendMessage(oocMessage, Util.DUMMY_UUID));
        return Command.SINGLE_SUCCESS;
    }

}
