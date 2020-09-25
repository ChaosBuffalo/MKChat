package com.chaosbuffalo.mkchat.event;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.capabilities.ChatCapabilities;
import com.chaosbuffalo.mkchat.capabilities.NpcDialogueProvider;
import com.chaosbuffalo.mkchat.capabilities.PlayerDialogueProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid= MKChat.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityHandler {

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof PlayerEntity) {
            e.addCapability(ChatCapabilities.PLAYER_DIALOGUE_CAP_ID,
                    new PlayerDialogueProvider((PlayerEntity) e.getObject()));
        } else if (e.getObject() instanceof LivingEntity){
            e.addCapability(ChatCapabilities.NPC_DIALOGUE_CAP_ID,
                    new NpcDialogueProvider((LivingEntity) e.getObject()));
        }
    }
}
