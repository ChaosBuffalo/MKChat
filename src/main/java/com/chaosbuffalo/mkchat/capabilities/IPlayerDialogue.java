package com.chaosbuffalo.mkchat.capabilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public interface IPlayerDialogue extends INBTSerializable<CompoundNBT> {

    void attach(PlayerEntity player);

    PlayerEntity getPlayer();

    PlayerNPCDialogueEntry getNPCEntry(UUID uuid);

    void cleanHistory();
}
