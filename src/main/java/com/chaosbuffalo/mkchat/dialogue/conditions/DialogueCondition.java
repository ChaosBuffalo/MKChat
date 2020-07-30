package com.chaosbuffalo.mkchat.dialogue.conditions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public abstract class DialogueCondition {
    private String conditionType;
    private boolean invert;

    public DialogueCondition(String conditionType){
        this.conditionType = conditionType;
        invert = false;
    }

    public abstract boolean meetsCondition(ServerPlayerEntity player, LivingEntity source);

    public boolean checkCondition(ServerPlayerEntity player, LivingEntity source){
        boolean condition = meetsCondition(player, source);
        return invert != condition;
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
    }
}
