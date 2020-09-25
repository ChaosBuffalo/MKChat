package com.chaosbuffalo.mkchat.capabilities;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NpcDialogueProvider implements ICapabilitySerializable<CompoundNBT> {

    private final NpcDialogueHandler data;

    public NpcDialogueProvider(LivingEntity entity){
        data = new NpcDialogueHandler();
        data.attach(entity);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return ChatCapabilities.NPC_DIALOGUE_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> data));
    }

    @Override
    public CompoundNBT serializeNBT() {
        return (CompoundNBT) ChatCapabilities.NPC_DIALOGUE_CAPABILITY.getStorage().writeNBT(
                ChatCapabilities.NPC_DIALOGUE_CAPABILITY, data, null);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        ChatCapabilities.NPC_DIALOGUE_CAPABILITY.getStorage().readNBT(
                ChatCapabilities.NPC_DIALOGUE_CAPABILITY, data, null, nbt);
    }


}
