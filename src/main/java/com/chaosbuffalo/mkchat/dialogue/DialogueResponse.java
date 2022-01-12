package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.InvalidCondition;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;

import java.util.ArrayList;
import java.util.List;

public class DialogueResponse {
    public static final String INVALID_RESPONSE_ID = "dialogue_response.invalid";
    private final List<DialogueCondition> conditions;
    private String responseNodeId;

    public DialogueResponse(String nodeId){
        this.responseNodeId = nodeId;
        this.conditions = new ArrayList<>();
    }

    public DialogueResponse(){
        this(INVALID_RESPONSE_ID);
    }

    public DialogueResponse copy(){
        DialogueResponse newResponse = new DialogueResponse();
        INBT nbt = serialize(NBTDynamicOps.INSTANCE);
        newResponse.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, nbt));
        return newResponse;
    }

    public boolean doesMatchConditions(ServerPlayerEntity player, LivingEntity source){
        return conditions.stream().allMatch(x -> x.checkCondition(player, source));
    }

    public List<DialogueCondition> getConditions() {
        return conditions;
    }

    public String getResponseNodeId() {
        return responseNodeId;
    }

    public boolean isValid(){
        return !responseNodeId.equals(INVALID_RESPONSE_ID);
    }

    public DialogueResponse addCondition(DialogueCondition condition){
        conditions.add(condition);
        return this;
    }

    public <D> void deserialize(Dynamic<D> dynamic) {
        this.responseNodeId = dynamic.get("responseNodeId").asString(INVALID_RESPONSE_ID);
        List<DialogueCondition> deserializedConditions = dynamic.get("conditions").asList(
                DialogueManager::deserializeCondition);
        conditions.clear();
        for (DialogueCondition cond : deserializedConditions){
            if (!cond.equals(InvalidCondition.INVALID_CONDITION)){
                conditions.add(cond);
            }
        }
    }

    public <D> D serialize(DynamicOps<D> ops) {
        D ret = ops.createMap(ImmutableMap.of(
                ops.createString("responseNodeId"), ops.createString(responseNodeId)
        ));
        if (conditions.size() > 0){
            ret = ops.mergeToMap(ret,
                    ops.createString("conditions"),
                    ops.createList(conditions.stream().map(x -> x.serialize(ops))))
                    .result().orElse(ret);
        }
        return ret;
    }
}
