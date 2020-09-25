package com.chaosbuffalo.mkchat.capabilities;

import com.chaosbuffalo.mkchat.MKChat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class ChatCapabilities {
    public static ResourceLocation PLAYER_DIALOGUE_CAP_ID = new ResourceLocation(MKChat.MODID,
            "player_dialogue_data");
    public static ResourceLocation NPC_DIALOGUE_CAP_ID = new ResourceLocation(MKChat.MODID,
            "npc_dialogue_data");


    @CapabilityInject(IPlayerDialogue.class)
    public static final Capability<IPlayerDialogue> PLAYER_DIALOGUE_CAPABILITY;

    @CapabilityInject(INpcDialogue.class)
    public static final Capability<INpcDialogue> NPC_DIALOGUE_CAPABILITY;

    static {
        PLAYER_DIALOGUE_CAPABILITY = null;
        NPC_DIALOGUE_CAPABILITY = null;
    }

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(IPlayerDialogue.class, new PlayerDialogueHandler.Storage(),
                PlayerDialogueHandler::new);
        CapabilityManager.INSTANCE.register(INpcDialogue.class, new NpcDialogueHandler.Storage(),
                NpcDialogueHandler::new);
    }
}
