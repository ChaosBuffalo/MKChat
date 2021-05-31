package com.chaosbuffalo.mkchat.dialogue.conditions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class InvalidCondition extends DialogueCondition {

    public static final InvalidCondition INVALID_CONDITION = new InvalidCondition();

    public InvalidCondition() {
        super(DialogueCondition.INVALID_CONDITION_TYPE);
    }

    @Override
    public boolean meetsCondition(ServerPlayerEntity player, LivingEntity source) {
        return false;
    }

    @Override
    public boolean checkCondition(ServerPlayerEntity player, LivingEntity source) {
        return false;
    }
}
