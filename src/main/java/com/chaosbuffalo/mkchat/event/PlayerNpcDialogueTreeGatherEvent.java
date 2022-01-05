package com.chaosbuffalo.mkchat.event;

import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.List;

public class PlayerNpcDialogueTreeGatherEvent extends PlayerEvent {

    private final List<DialogueTree> treeStack;
    private final LivingEntity speaker;


    public PlayerNpcDialogueTreeGatherEvent(PlayerEntity player, LivingEntity speaker, List<DialogueTree> treeStack) {
        super(player);
        this.treeStack = treeStack;
        this.speaker = speaker;
    }

    public LivingEntity getSpeaker() {
        return speaker;
    }

    public void addTree(DialogueTree tree){
        treeStack.add(tree);
    }
}
