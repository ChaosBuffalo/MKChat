package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.MKChat;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.function.Function;

public class ContextStringTextComponent extends StringTextComponent {
    private final Function<DialogueContext, List<Object>> argsSupplier;

    public ContextStringTextComponent(String msg, Function<DialogueContext, List<Object>> argsSupplier) {
        super(msg);
        this.argsSupplier = argsSupplier;
    }

    public ITextComponent getContextFormattedTextComponent(DialogueContext context){
        MKChat.LOGGER.info("Applying {}, {}", getText(), argsSupplier.apply(context));
        return new StringTextComponent(String.format(getText(), argsSupplier.apply(context).toArray()));
    }
}
