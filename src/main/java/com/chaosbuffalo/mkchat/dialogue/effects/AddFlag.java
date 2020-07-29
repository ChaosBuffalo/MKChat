package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.capabilities.Capabilities;
import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class AddFlag extends DialogueEffect{
    public static String effectTypeName = "add_flag";
    private String flagName;

    public AddFlag(String flagName){
        super(effectTypeName);
        this.flagName = flagName;
    }

    public AddFlag(){
        this("default");
    }

    @Override
    public void applyEffect(ServerPlayerEntity player, LivingEntity source, DialogueNode node) {
        player.getCapability(Capabilities.PLAYER_DIALOGUE_CAPABILITY).ifPresent(cap ->
                cap.getNPCEntry(source.getUniqueID()).putBoolFlag(flagName, true));
    }
}
