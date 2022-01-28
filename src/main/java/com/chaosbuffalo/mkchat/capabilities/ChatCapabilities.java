package com.chaosbuffalo.mkchat.capabilities;

import com.chaosbuffalo.mkchat.MKChat;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

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
        CapabilityManager.INSTANCE.register(IPlayerDialogue.class, new NBTStorage<>(), PlayerDialogueHandler::new);
        CapabilityManager.INSTANCE.register(INpcDialogue.class, new NBTStorage<>(), NpcDialogueHandler::new);
    }

    public static class NBTStorage<T extends INBTSerializable<CompoundNBT>> implements Capability.IStorage<T> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<T> capability, T instance, Direction side) {
            if (instance == null) {
                return null;
            }
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT && instance != null) {
                CompoundNBT tag = (CompoundNBT) nbt;
                instance.deserializeNBT(tag);
            }
        }
    }
}
