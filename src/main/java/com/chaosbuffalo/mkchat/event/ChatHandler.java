package com.chaosbuffalo.mkchat.event;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.entity.IPlayerChatReceiver;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MKChat.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChatHandler {
    private static final double CHAT_RADIUS = 5.0;

    private static AxisAlignedBB getChatBoundingBox(ServerPlayerEntity entity){
        return new AxisAlignedBB(new BlockPos(entity)).grow(CHAT_RADIUS, entity.getHeight(), CHAT_RADIUS);
    }

    @SubscribeEvent
    public static void handleServerChat(ServerChatEvent event){
        ServerPlayerEntity player = event.getPlayer();
        if (player.getServer() != null){
            player.getServer().getPlayerList().sendToAllNearExcept(null,
                    player.getPosX(), player.getPosY(), player.getPosZ(), CHAT_RADIUS,
                    player.dimension,
                    new SChatPacket(event.getComponent(), ChatType.CHAT));
            List<LivingEntity> entities = player.getServerWorld().getEntitiesWithinAABB(LivingEntity.class, getChatBoundingBox(player),
                    (x) -> x instanceof IPlayerChatReceiver && x.canEntityBeSeen(player));
            for (LivingEntity entity : entities){
                ((IPlayerChatReceiver) entity).receiveMessage(player, event.getMessage());
            }
        }
        event.setCanceled(true);
    }
}
