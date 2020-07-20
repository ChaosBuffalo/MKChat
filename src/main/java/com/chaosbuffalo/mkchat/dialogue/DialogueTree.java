package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DialogueTree {
    private final String dialogueName;
    private final Map<UUID, DialogueNode> nodes;
    private final Map<UUID, DialoguePrompt> prompts;
    private DialogueNode startNode;

    public DialogueTree(String dialogueName){
        this.dialogueName = dialogueName;
        this.nodes = new HashMap<>();
        this.prompts = new HashMap<>();
    }

    public DialogueNode addNode(ITextComponent message){
        DialogueNode newNode = new DialogueNode(UUID.randomUUID(), message);
        nodes.put(newNode.getNodeId(), newNode);
        return newNode;
    }

    public void setStartNode(DialogueNode startNode) {
        this.startNode = startNode;
    }

    @Nullable
    public DialogueNode getStartNode() {
        return startNode;
    }

    public DialoguePrompt addPrompt(String promptPhrase, String defaultPromptText){
        DialoguePrompt prompt = new DialoguePrompt(UUID.randomUUID(), promptPhrase, defaultPromptText);
        prompts.put(prompt.getPromptId(), prompt);
        return prompt;
    }

    public void handlePlayerMessage(ServerPlayerEntity player, String message, LivingEntity speaker){
        for (DialoguePrompt prompt : prompts.values()){
            if (prompt.doesMatchInput(message)){
                prompt.handlePrompt(player, speaker);
            }
        }
    }
}
