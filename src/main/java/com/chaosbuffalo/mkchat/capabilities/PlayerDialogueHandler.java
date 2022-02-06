package com.chaosbuffalo.mkchat.capabilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDialogueHandler implements IPlayerDialogue {
    private final PlayerEntity player;
    private final Map<UUID, PlayerConversationMemory> npcEntries;

    public PlayerDialogueHandler(PlayerEntity player) {
        this.player = player;
        this.npcEntries = new HashMap<>();
    }

    @Override
    public PlayerEntity getPlayer() {
        return player;
    }

    public PlayerConversationMemory getConversationMemory(UUID uuid) {
        return npcEntries.computeIfAbsent(uuid, PlayerConversationMemory::new);
    }

    @Override
    public void cleanHistory() {
        npcEntries.clear();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT npcEntriesTag = new CompoundNBT();
        for (Map.Entry<UUID, PlayerConversationMemory> entry : npcEntries.entrySet()) {
            npcEntriesTag.put(entry.getKey().toString(), entry.getValue().serializeNBT());
        }
        tag.put("npcEntries", npcEntriesTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        CompoundNBT npcEntriesTag = nbt.getCompound("npcEntries");
        npcEntries.clear();
        for (String key : npcEntriesTag.keySet()) {
            UUID uuid = UUID.fromString(key);
            PlayerConversationMemory dialogueEntry = new PlayerConversationMemory(uuid);
            dialogueEntry.deserializeNBT(npcEntriesTag.getCompound(key));
            npcEntries.put(uuid, dialogueEntry);
        }
    }
}
