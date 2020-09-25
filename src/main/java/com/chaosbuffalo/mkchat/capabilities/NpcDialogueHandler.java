package com.chaosbuffalo.mkchat.capabilities;

import com.chaosbuffalo.mkchat.ChatConstants;
import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.DialogueManager;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class NpcDialogueHandler implements INpcDialogue{

    private LivingEntity entity;
    private ResourceLocation dialogueName;

    public NpcDialogueHandler(){

    }

    @Override
    public void attach(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean hasDialogue() {
        return dialogueName != null;
    }

    @Override
    public void receiveMessage(ServerPlayerEntity player, String message) {
        if (hasDialogue()){
            DialogueTree tree = DialogueManager.getDialogueTree(getDialogueTreeName());
            if (tree != null){
                tree.handlePlayerMessage(player, message, entity);
            }
        }
    }

    @Override
    public void startDialogue(ServerPlayerEntity player) {
        DialogueTree tree = DialogueManager.getDialogueTree(getDialogueTreeName());
        if (tree != null && tree.getHailPrompt() != null) {
            if (player.getServer() != null){
                player.getServer().getPlayerList().sendToAllNearExcept(null,
                        player.getPosX(), player.getPosY(), player.getPosZ(), ChatConstants.CHAT_RADIUS,
                        player.dimension,
                        new SChatPacket(new StringTextComponent(String.format("<%s> Hail, %s",
                                player.getName().getFormattedText(), this.getEntity().getName().getFormattedText())),
                                ChatType.CHAT));
            }
            tree.getHailPrompt().handlePrompt(player, entity, tree);
        } else {
            MKChat.LOGGER.info("Failed to find dialogue {}", getDialogueTreeName());
        }
    }

    @Override
    public void setDialogueTree(ResourceLocation treeName) {
        dialogueName = treeName;
    }

    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    @Nullable
    @Override
    public ResourceLocation getDialogueTreeName() {
        return dialogueName;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return new CompoundNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {

    }

    public static class Storage implements Capability.IStorage<INpcDialogue> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<INpcDialogue> capability, INpcDialogue instance, Direction side) {
            if (instance == null){
                return null;
            }
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<INpcDialogue> capability, INpcDialogue instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT && instance != null) {
                CompoundNBT tag = (CompoundNBT) nbt;
                instance.deserializeNBT(tag);
            }
        }
    }
}
