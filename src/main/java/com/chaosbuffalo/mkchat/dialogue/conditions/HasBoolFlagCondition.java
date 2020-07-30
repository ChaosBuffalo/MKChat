package com.chaosbuffalo.mkchat.dialogue.conditions;

import com.chaosbuffalo.mkchat.capabilities.Capabilities;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class HasBoolFlagCondition extends DialogueCondition {
    public static final String conditionTypeName = "has_bool_flag";
    private String flagName;

    public HasBoolFlagCondition(String flagName){
        super(conditionTypeName);
        this.flagName = flagName;
    }

    public HasBoolFlagCondition(){
        this("default");
    }

    @Override
    public boolean meetsCondition(ServerPlayerEntity player, LivingEntity source) {
        return player.getCapability(Capabilities.PLAYER_DIALOGUE_CAPABILITY).map(cap ->
                cap.getNPCEntry(source.getUniqueID()).getBoolFlag(flagName)).orElse(false);
    }
}
