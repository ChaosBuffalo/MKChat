package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.mojang.serialization.Dynamic;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class InvalidEffect extends DialogueEffect {
    public final static InvalidEffect INVALID_EFFECT = new InvalidEffect();

    public InvalidEffect(){
        super(DialogueEffect.INVALID_EFFECT_TYPE);
    }

    @Override
    public void applyEffect(ServerPlayerEntity player, LivingEntity source, DialogueNode node) {

    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {

    }
}
