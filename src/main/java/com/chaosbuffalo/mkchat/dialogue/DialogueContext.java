package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class DialogueContext {
    private final LivingEntity speaker;
    private final ServerPlayerEntity player;
    private final DialogueObject dialogueObject;

    public DialogueContext(LivingEntity speaker, ServerPlayerEntity player, DialogueObject dialogueObject) {
        this.speaker = speaker;
        this.player = player;
        this.dialogueObject = dialogueObject;
    }

    public DialogueObject getDialogueObject() {
        return dialogueObject;
    }

    public LivingEntity getSpeaker() {
        return speaker;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }
}
