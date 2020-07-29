package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public abstract class DialogueEffect {

    private final String effectType;

    public DialogueEffect(String effectType){
        this.effectType = effectType;
    }

    public abstract void applyEffect(ServerPlayerEntity player, LivingEntity source, DialogueNode node);
}
