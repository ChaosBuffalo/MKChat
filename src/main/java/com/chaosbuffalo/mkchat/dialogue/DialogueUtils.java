package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.MKChat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;

public class DialogueUtils {
    private static final double CHAT_RADIUS = 5.0;

    public static void sendMessageToAllAround(MinecraftServer server, LivingEntity source,
                                              Component message) {
        server.getPlayerList().broadcast(null,
                source.getX(), source.getY(), source.getZ(), CHAT_RADIUS,
                source.getCommandSenderWorld().dimension(),
                new ClientboundChatPacket(message, ChatType.CHAT, Util.NIL_UUID));

    }

    public static String getItemNameProvider(Item item) {
        return String.format("{item:%s}", item.getRegistryName().toString());
    }

    public static String getStackCountItemProvider(ItemStack item) {
        return String.format("%d {item:%s}", item.getCount(), item.getItem().getRegistryName().toString());
    }

    public static void throwParseException(String message) {
        MKChat.LOGGER.error(message);
        throw new DialogueDataParsingException(message);
    }
}
