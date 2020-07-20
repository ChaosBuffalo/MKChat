package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class DialogueString {
    private final String text;

    public DialogueString(String text){
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public ITextComponent getTextComponent(){
        return new StringTextComponent(text);
    }
}
