package com.chaosbuffalo.mkchat.capabilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDialogueHandler implements IPlayerDialogue {
    private PlayerEntity player;
    private final Map<UUID, PlayerNPCDialogueEntry> npcEntries;

    public PlayerDialogueHandler(){
        this.npcEntries = new HashMap<>();
    }

    public PlayerNPCDialogueEntry getNPCEntry(UUID uuid){
        return npcEntries.computeIfAbsent(uuid, PlayerNPCDialogueEntry::new);
    }

    @Override
    public void attach(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public PlayerEntity getPlayer() {
        return player;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT npcEntriesTag = new CompoundNBT();
        for (Map.Entry<UUID, PlayerNPCDialogueEntry> entry : npcEntries.entrySet()){
            npcEntriesTag.put(entry.getKey().toString(), entry.getValue().serializeNBT());
        }
        tag.put("npcEntries", npcEntriesTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        CompoundNBT npcEntriesTag = nbt.getCompound("npcEntries");
        npcEntries.clear();
        for (String key : npcEntriesTag.keySet()){
            UUID uuid = UUID.fromString(key);
            PlayerNPCDialogueEntry dialogueEntry = new PlayerNPCDialogueEntry(uuid);
            dialogueEntry.deserializeNBT(npcEntriesTag.getCompound(key));
            npcEntries.put(uuid, dialogueEntry);
        }
    }

    public static class Storage implements Capability.IStorage<IPlayerDialogue> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<IPlayerDialogue> capability, IPlayerDialogue instance, Direction side) {
            if (instance == null){
                return null;
            }
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<IPlayerDialogue> capability, IPlayerDialogue instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT && instance != null) {
                CompoundNBT tag = (CompoundNBT) nbt;
                instance.deserializeNBT(tag);
            }
        }
    }
}
