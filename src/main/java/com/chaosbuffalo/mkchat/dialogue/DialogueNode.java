package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.InvalidEffect;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
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

    public DialogueNode(){
        this(INVALID_OBJECT, EMPTY_MSG);
    }

    public DialogueNode(String nodeId){
        this(nodeId, EMPTY_MSG);
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

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        D ret = super.serialize(ops);
        if (effects.size() > 0){
            ret = ops.mergeToMap(ret, ImmutableMap.of(
                    ops.createString("effects"), ops.createList(effects.stream().map(x -> x.serialize(ops)))
            )).result().orElse(ret);
        }
        return ret;
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        super.deserialize(dynamic);
        List<DialogueEffect> deserializedEffects = dynamic.get("effects").asList(DialogueManager::deserializeEffect);
        effects.clear();
        for (DialogueEffect effect : deserializedEffects){
            if (!effect.equals(InvalidEffect.INVALID_EFFECT)){
                effects.add(effect);
            }
        }
    }
}
