package com.chaosbuffalo.mkchat.entity;

import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class DialogueComponent {
    private final LivingEntity entity;
    private final DialogueTree dialogueTree;

    public DialogueComponent(LivingEntity entity, DialogueTree tree){
        this.entity = entity;
        this.dialogueTree = tree;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public void startDialogue(ServerPlayerEntity player){
        if (dialogueTree.getStartNode() != null) {
            dialogueTree.getStartNode().sendMessage(player, entity);
        }
    }

    public void receiveMessageFromPlayer(ServerPlayerEntity playerEntity, String msg){
        dialogueTree.handlePlayerMessage(playerEntity, msg, entity);
    }
}
