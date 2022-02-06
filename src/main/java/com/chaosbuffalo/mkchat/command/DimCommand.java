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

public class DimCommand {
    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("dim").then(Commands.argument("msg", StringArgumentType.greedyString())
                .executes(DimCommand::handleMessage));
    }

    static int handleMessage(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        String msg = StringArgumentType.getString(ctx, "msg");
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        StringTextComponent compMessage = new StringTextComponent(String.format("[Dim]<%s>: %s",
                player.getName().getString(), msg));
        compMessage.mergeStyle(TextFormatting.GOLD);
        // emulate sendMessage but only to players in the dimension
        ctx.getSource().getServer().sendMessage(compMessage, Util.DUMMY_UUID);
        player.getServerWorld().getPlayers().forEach(
                playerEntity -> playerEntity.sendMessage(compMessage, Util.DUMMY_UUID));
        return Command.SINGLE_SUCCESS;
    }
}
