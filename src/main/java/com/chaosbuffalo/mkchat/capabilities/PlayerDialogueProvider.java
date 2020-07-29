package com.chaosbuffalo.mkchat.capabilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerDialogueProvider implements ICapabilitySerializable<CompoundNBT> {

    private final PlayerDialogueHandler data;

    public PlayerDialogueProvider(PlayerEntity player){
        data = new PlayerDialogueHandler();
        data.attach(player);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return Capabilities.PLAYER_DIALOGUE_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> data));
    }

    @Override
    public CompoundNBT serializeNBT() {
        return (CompoundNBT) Capabilities.PLAYER_DIALOGUE_CAPABILITY.getStorage().writeNBT(
                Capabilities.PLAYER_DIALOGUE_CAPABILITY, data, null);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        Capabilities.PLAYER_DIALOGUE_CAPABILITY.getStorage().readNBT(
                Capabilities.PLAYER_DIALOGUE_CAPABILITY, data, null, nbt);
    }


}