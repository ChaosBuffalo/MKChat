package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class AddLevelEffect extends DialogueEffect {
    private int levelAmount;
    public static String effectTypeName = "add_level";

    public AddLevelEffect(int levelAmount){
        super(effectTypeName);
        this.levelAmount = levelAmount;
    }

    public AddLevelEffect(){
        this(0);
    }

    @Override
    public void applyEffect(ServerPlayerEntity player, LivingEntity source, DialogueNode node) {
        player.addExperienceLevel(levelAmount);
    }
}
