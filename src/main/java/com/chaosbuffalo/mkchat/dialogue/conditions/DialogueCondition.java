package com.chaosbuffalo.mkchat.dialogue.conditions;

import com.chaosbuffalo.mkchat.MKChat;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

public abstract class DialogueCondition {
    public static final ResourceLocation INVALID_CONDITION_TYPE = new ResourceLocation(MKChat.MODID, "dialogue_condition.invalid");
    private ResourceLocation conditionType;
    private boolean invert;

    public DialogueCondition(ResourceLocation conditionType){
        this.conditionType = conditionType;
        invert = false;
    }

    public abstract boolean meetsCondition(ServerPlayerEntity player, LivingEntity source);

    public boolean checkCondition(ServerPlayerEntity player, LivingEntity source){
        boolean condition = meetsCondition(player, source);
        return invert != condition;
    }

    public DialogueCondition setInvert(boolean invert) {
        this.invert = invert;
        return this;
    }

    public <D> D serialize(DynamicOps<D> ops){
        return ops.createMap(ImmutableMap.of(
                ops.createString("dialogueConditionType"), ops.createString(conditionType.toString()),
                ops.createString("invert"), ops.createBoolean(invert)
        ));
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic){
        return new ResourceLocation(dynamic.get("dialogueConditionType").asString().result().orElse(INVALID_CONDITION_TYPE.toString()));
    }

    public <D> void deserialize(Dynamic<D> dynamic){
        invert = dynamic.get("invert").asBoolean(false);
    }
}
