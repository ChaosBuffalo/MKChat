package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class DialogueNode extends DialogueObject{
    private final List<DialogueEffect> effects;

    public DialogueNode(String nodeId, String rawMessage){
        super(nodeId, rawMessage);
        this.effects = new ArrayList<>();
    }

    public void addEffect(DialogueEffect effect){
        this.effects.add(effect);
    }

    public void sendMessage(ServerPlayerEntity player, LivingEntity source){
        if (player.getServer() != null){
            DialogueUtils.sendMessageToAllAround(player.getServer(), source, getMessage(source, player));
            for (DialogueEffect effect : effects){
                effect.applyEffect(player, source, this);
            }
        }
    }
}
