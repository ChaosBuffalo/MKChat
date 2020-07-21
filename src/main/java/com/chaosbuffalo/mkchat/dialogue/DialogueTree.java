package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class DialogueTree {
    private final ResourceLocation dialogueName;
    private final Map<String, DialogueNode> nodes;
    private final Map<String, DialoguePrompt> prompts;
    private DialogueNode startNode;

    public DialogueTree(ResourceLocation dialogueName){
        this.dialogueName = dialogueName;
        this.nodes = new HashMap<>();
        this.prompts = new HashMap<>();
    }

    public void addNode(DialogueNode node){
        nodes.put(node.getNodeId(), node);
    }

    public void setStartNode(DialogueNode startNode) {
        this.startNode = startNode;
    }

    public ResourceLocation getDialogueName() {
        return dialogueName;
    }

    @Nullable
    public DialogueNode getStartNode() {
        return startNode;
    }

    public void addPrompt(DialoguePrompt prompt){
        prompts.put(prompt.getPromptId(), prompt);
    }


    public void handlePlayerMessage(ServerPlayerEntity player, String message, LivingEntity speaker){
        for (DialoguePrompt prompt : prompts.values()){
            if (prompt.doesMatchInput(message)){
                prompt.handlePrompt(player, speaker);
            }
        }
    }
}
