package com.chaosbuffalo.mkchat.entity;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;

public interface IPlayerChatReceiver {

    void receiveMessage(ServerPlayerEntity player, String message);
}
