package com.chaosbuffalo.mkchat.entity;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.init.ChatEntityTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;

public class TestChatReceiverEntity extends PigEntity implements IPlayerChatReceiver{

    public TestChatReceiverEntity(final EntityType<? extends TestChatReceiverEntity> entityType, World world) {
        super(entityType, world);
    }

    public TestChatReceiverEntity(World world){
        super(ChatEntityTypes.TEST_CHAT.get(), world);
    }


    @Override
    public void receiveMessage(ServerPlayerEntity player, String message) {
        MKChat.LOGGER.info("Received chat message from {}, {}", player, message);
    }
}
