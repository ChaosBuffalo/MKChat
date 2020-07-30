package com.chaosbuffalo.mkchat.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

public class ChatCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(OOCCommand.register());
        dispatcher.register(DimCommand.register());
        dispatcher.register(PartyCommand.register());
        dispatcher.register(CleanHistoryCommand.register());
    }
}
