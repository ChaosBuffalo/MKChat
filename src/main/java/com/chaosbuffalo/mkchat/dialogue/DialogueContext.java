package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class DialogueContext {
    private final LivingEntity speaker;
    private final ServerPlayerEntity player;
    private final DialogueNode node;

    public DialogueContext(LivingEntity speaker, ServerPlayerEntity player, DialogueNode node){
        this.speaker = speaker;
        this.player = player;
        this.node = node;
    }

    public DialogueNode getNode() {
        return node;
    }

    public LivingEntity getSpeaker() {
        return speaker;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }
}
