package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

public abstract class DialogueEffect {
    public final static ResourceLocation INVALID_EFFECT_TYPE = new ResourceLocation(MKChat.MODID, "dialogue_effect.invalid");
    private final ResourceLocation effectType;

    public DialogueEffect(ResourceLocation effectType){
        this.effectType = effectType;
    }

    public abstract void applyEffect(ServerPlayerEntity player, LivingEntity source, DialogueNode node);

    public <D> D serialize(DynamicOps<D> ops){
        return ops.createMap(ImmutableMap.of(
                ops.createString("dialogueEffectType"), ops.createString(effectType.toString())
        ));
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic){
        return new ResourceLocation(dynamic.get("dialogueEffectType").asString().result().orElse(INVALID_EFFECT_TYPE.toString()));
    }

    public abstract <D> void deserialize(Dynamic<D> dynamic);
}
