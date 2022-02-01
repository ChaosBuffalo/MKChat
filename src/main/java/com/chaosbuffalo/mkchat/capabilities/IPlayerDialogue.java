package com.chaosbuffalo.mkchat.capabilities;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.UUID;

public interface IPlayerDialogue extends INBTSerializable<CompoundNBT> {

    PlayerEntity getPlayer();

    PlayerConversationMemory getConversationMemory(UUID uuid);

    default PlayerConversationMemory getConversationMemory(LivingEntity target) {
        return getConversationMemory(target.getUniqueID());
    }

    void cleanHistory();

    static LazyOptional<IPlayerDialogue> get(PlayerEntity player) {
        return player.getCapability(ChatCapabilities.PLAYER_DIALOGUE_CAPABILITY);
    }
}
