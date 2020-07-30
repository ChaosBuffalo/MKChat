package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

import java.util.ArrayList;
import java.util.List;

public class DialoguePrompt extends DialogueObject {
    private final String promptPhrase;
    private final String defaultPromptText;
    private final List<DialogueResponse> responses;

    public DialoguePrompt(String promptId, String promptPhrase, String defaultPromptText, String text){
        super(promptId, text);
        this.promptPhrase = promptPhrase;
        this.defaultPromptText = defaultPromptText;
        this.responses = new ArrayList<>();
    }

    public void addResponse(DialogueResponse response){
        responses.add(response);
    }

    public String getPromptPhrase() {
        return promptPhrase;
    }

    public String getDefaultPromptText() {
        return defaultPromptText;
    }

    public boolean doesMatchInput(String input){
        return !promptPhrase.equals("") && input.contains(promptPhrase);
    }

    public void handlePrompt(ServerPlayerEntity player, LivingEntity source, DialogueTree tree){
        for (DialogueResponse response : responses){
            if (response.doesMatchConditions(player, source)){
                DialogueNode responseNode = tree.getNode(response.getResponseNodeId());
                if (responseNode != null){
                    responseNode.sendMessage(player, source);
                    return;
                }
            }
        }
    }

    public ITextComponent getPromptLink() {
        ITextComponent textComponent = new StringTextComponent(String.format("[%s]",
                getMessage().getFormattedText()));
        textComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                getDefaultPromptText()));
        textComponent.getStyle().setColor(TextFormatting.AQUA);
        return textComponent;
    }
}
