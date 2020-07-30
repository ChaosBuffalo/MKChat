package com.chaosbuffalo.mkchat.entity;

import net.minecraft.entity.player.ServerPlayerEntity;

public interface IPlayerChatReceiver {

    void receiveMessage(ServerPlayerEntity player, String message);
}
