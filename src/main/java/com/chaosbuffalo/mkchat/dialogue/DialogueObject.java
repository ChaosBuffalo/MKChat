package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.MKChat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DialogueObject {
    private final String rawMessage;
    private ITextComponent message;
    private final String id;
    private DialogueTree dialogueTree;

    public DialogueObject(String id, String rawMessage){
        this.id = id;
        this.rawMessage = rawMessage;
    }

    public void setDialogueTree(DialogueTree dialogueTree) {
        this.dialogueTree = dialogueTree;
    }

    public String getId() {
        return id;
    }

    public DialogueTree getDialogueTree() {
        return dialogueTree;
    }

    public ITextComponent getMessage() {
        return message;
    }

    public void compileMessage(){
        this.message = DialogueManager.parseDialogueMessage(rawMessage, dialogueTree);
    }

    public ITextComponent getMessage(LivingEntity source, ServerPlayerEntity target) {
        ITextComponent name = new StringTextComponent(String.format("<%s> ",
                source.getDisplayName().getFormattedText()));
        DialogueContext context = new DialogueContext(source, target, this);
        Stream<ITextComponent> finalMsg = message.getSiblings().stream().map((comp -> {
            if (comp instanceof ContextAwareTextComponent){
                return ((ContextAwareTextComponent) comp).getContextFormattedTextComponent(context);
            } else {
                return comp;
            }
        }));
        for (ITextComponent comp : finalMsg.collect(Collectors.toList())){
            MKChat.LOGGER.info("Comp: {}", comp);
            name.appendSibling(comp);
        }
        return name;
    }
}
