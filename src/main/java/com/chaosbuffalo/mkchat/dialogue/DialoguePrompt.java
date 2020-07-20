package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public class DialoguePrompt {
    private final String promptId;
    private final String promptPhrase;
    private final String defaultPromptText;
    private DialogueNode resultNode;

    public DialoguePrompt(String promptId, String promptPhrase, String defaultPromptText){
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

    public String getPromptId() {
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

    public ITextComponent getTextComponent() {
        ITextComponent textComponent = new StringTextComponent(String.format("[%s]", getPromptPhrase()));
        textComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                getDefaultPromptText()));
        textComponent.getStyle().setColor(TextFormatting.AQUA);
        return textComponent;
    }
}
