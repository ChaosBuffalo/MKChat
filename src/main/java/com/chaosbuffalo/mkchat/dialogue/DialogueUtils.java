package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;

public class DialogueUtils {
    private static final double CHAT_RADIUS = 5.0;

    public static void sendMessageToAllAround(MinecraftServer server, LivingEntity source,
                                              ITextComponent message){
        server.getPlayerList().sendToAllNearExcept(null,
                source.getPosX(), source.getPosY(), source.getPosZ(), CHAT_RADIUS,
                source.getEntityWorld().getDimensionKey(),
                new SChatPacket(message, ChatType.CHAT, Util.DUMMY_UUID));

    }
}
