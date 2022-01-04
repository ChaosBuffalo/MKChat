package com.chaosbuffalo.mkchat.event;

import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Stack;

public class PlayerNpcDialogueTreeStackSetupEvent extends PlayerEvent {

    private final Stack<DialogueTree> treeStack;
    private final LivingEntity speaker;


    public PlayerNpcDialogueTreeStackSetupEvent(PlayerEntity player, LivingEntity speaker, Stack<DialogueTree> treeStack) {
        super(player);
        this.treeStack = treeStack;
        this.speaker = speaker;
    }

    public LivingEntity getSpeaker() {
        return speaker;
    }

    public void addTree(DialogueTree tree){
        treeStack.push(tree);
    }
}
