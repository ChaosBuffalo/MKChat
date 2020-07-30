package com.chaosbuffalo.mkchat.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerNPCDialogueEntry implements INBTSerializable<CompoundNBT> {
    private UUID uuid;
    private final Map<String, Boolean> boolFlags;

    public PlayerNPCDialogueEntry(UUID uuid){
        this.uuid = uuid;
        this.boolFlags = new HashMap<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void putBoolFlag(String key, boolean value){
        boolFlags.put(key, value);
    }

    public boolean getBoolFlag(String key){
        return boolFlags.getOrDefault(key, false);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putUniqueId("npcId", uuid);
        CompoundNBT boolFlagsTag = new CompoundNBT();
        for (Map.Entry<String, Boolean> entry : boolFlags.entrySet()){
            boolFlagsTag.putBoolean(entry.getKey(), entry.getValue());
        }
        tag.put("boolFlags", boolFlagsTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        uuid = nbt.getUniqueId("npcId");
        boolFlags.clear();
        CompoundNBT boolFlagsTag = nbt.getCompound("boolFlags");
        for (String key : boolFlagsTag.keySet()){
            boolean value = boolFlagsTag.getBoolean(key);
            boolFlags.put(key, value);
        }
    }
}
