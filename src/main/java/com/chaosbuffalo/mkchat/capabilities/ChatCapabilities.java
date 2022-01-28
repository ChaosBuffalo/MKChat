package com.chaosbuffalo.mkchat.capabilities;

import com.chaosbuffalo.mkchat.MKChat;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
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
        CapabilityManager.INSTANCE.register(IPlayerDialogue.class, new NBTStorage<>(), () -> null);
        CapabilityManager.INSTANCE.register(INpcDialogue.class, new NBTStorage<>(), () -> null);
    }

    public abstract static class Provider<CapTarget, CapType extends INBTSerializable<CompoundNBT>> implements ICapabilitySerializable<CompoundNBT> {

        private final CapType data;
        private final LazyOptional<CapType> capOpt;

        public Provider(CapTarget attached) {
            data = makeData(attached);
            capOpt = LazyOptional.of(() -> data);
        }

        abstract CapType makeData(CapTarget attached);

        abstract Capability<CapType> getCapability();

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return getCapability().orEmpty(cap, capOpt);
        }

        public void invalidate() {
            capOpt.invalidate();
        }

        @Override
        public CompoundNBT serializeNBT() {
            return data.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            data.deserializeNBT(nbt);
        }
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
