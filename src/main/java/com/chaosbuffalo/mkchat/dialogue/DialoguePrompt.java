package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public class DialoguePrompt {
    private final UUID promptId;
    private final String promptPhrase;
    private final String defaultPromptText;
    private DialogueNode resultNode;

    public DialoguePrompt(UUID promptId, String promptPhrase, String defaultPromptText){
        this.promptId = promptId;
        this.promptPhrase = promptPhrase;
        this.defaultPromptText = defaultPromptText;
        resultNode = null;
    }

    public void setResultNode(DialogueNode resultNode) {
        this.resultNode = resultNode;
    }

    public DialogueNode getResultNode() {
        return resultNode;
    }

    public String getPromptPhrase() {
        return promptPhrase;
    }

    public UUID getPromptId() {
        return promptId;
    }

    public String getDefaultPromptText() {
        return defaultPromptText;
    }

    public boolean doesMatchInput(String input){
        return input.contains(promptPhrase);
    }

    public void handlePrompt(ServerPlayerEntity player, LivingEntity source){
        if (getResultNode() != null){
            getResultNode().sendMessage(player, source);
        }

    }
}
