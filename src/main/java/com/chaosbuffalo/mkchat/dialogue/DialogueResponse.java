package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class DialogueResponse {

    private final List<DialogueCondition> conditions;
    private final String responseNodeId;

    public DialogueResponse(String nodeId){
        this.responseNodeId = nodeId;
        this.conditions = new ArrayList<>();
    }

    public boolean doesMatchConditions(ServerPlayerEntity player, LivingEntity source){
        return conditions.stream().allMatch(x -> x.checkCondition(player, source));
    }

    public String getResponseNodeId() {
        return responseNodeId;
    }

    public void addCondition(DialogueCondition condition){
        conditions.add(condition);
    }
}
