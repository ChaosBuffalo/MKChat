package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public class PromptString extends DialogueString{
    private final DialoguePrompt prompt;

    public PromptString(DialoguePrompt prompt, String text) {
        super(text);
        this.prompt = prompt;
    }

    @Override
    public ITextComponent getTextComponent() {
        ITextComponent textComponent = new StringTextComponent(String.format("[%s]", getText()));
        textComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                prompt.getDefaultPromptText()));
        textComponent.getStyle().setColor(TextFormatting.AQUA);
        return textComponent;
    }
}
