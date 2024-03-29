package com.chaosbuffalo.mkchat.capabilities;

import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public interface INpcDialogue extends INBTSerializable<CompoundNBT> {

    boolean hasDialogue();

    void addAdditionalDialogueTree(DialogueTree tree);

    void receiveMessage(ServerPlayerEntity player, String message);

    void startDialogue(ServerPlayerEntity player);

    @Deprecated
    void startDialogue(ServerPlayerEntity player, boolean suppressHail);

    default void hail(ServerPlayerEntity player) {
        startDialogue(player);
    }

    void setDialogueTree(ResourceLocation treeName);

    LivingEntity getEntity();

    @Nullable
    ResourceLocation getDialogueTreeName();

    static LazyOptional<INpcDialogue> get(LivingEntity entity) {
        return entity.getCapability(ChatCapabilities.NPC_DIALOGUE_CAPABILITY);
    }
}
