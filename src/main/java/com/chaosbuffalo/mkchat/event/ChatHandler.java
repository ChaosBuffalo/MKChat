package com.chaosbuffalo.mkchat.event;

import com.chaosbuffalo.mkchat.ChatConstants;
import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.capabilities.ChatCapabilities;
import com.chaosbuffalo.mkchat.capabilities.INpcDialogue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MKChat.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChatHandler {


    private static AxisAlignedBB getChatBoundingBox(ServerPlayerEntity entity){
        return new AxisAlignedBB(new BlockPos(entity.getPosition())).grow(ChatConstants.CHAT_RADIUS, entity.getHeight(), ChatConstants.CHAT_RADIUS);
    }

    @SubscribeEvent
    public static void handleServerChat(ServerChatEvent event){
        ServerPlayerEntity player = event.getPlayer();
        if (player.getServer() != null){
            player.getServer().getPlayerList().sendToAllNearExcept(null,
                    player.getPosX(), player.getPosY(), player.getPosZ(), ChatConstants.CHAT_RADIUS,
                    player.getServerWorld().getDimensionKey(),
                    new SChatPacket(event.getComponent(), ChatType.CHAT, Util.DUMMY_UUID));
            List<LivingEntity> entities = player.getServerWorld().getEntitiesWithinAABB(LivingEntity.class, getChatBoundingBox(player),
                    (x) -> x.canEntityBeSeen(player) && x.getCapability(ChatCapabilities.NPC_DIALOGUE_CAPABILITY)
                            .map(INpcDialogue::hasDialogue).orElse(false));
            for (LivingEntity entity : entities){
                entity.getCapability(ChatCapabilities.NPC_DIALOGUE_CAPABILITY).ifPresent(cap -> {
                    cap.receiveMessage(player, event.getMessage());
                });
            }
        }
        event.setCanceled(true);
    }
}
