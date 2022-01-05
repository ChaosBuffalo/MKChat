package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.MKChat;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DialogueObject {
    public static final String INVALID_OBJECT = "invalid";
    public static final String EMPTY_MSG = "";
    private String rawMessage;
    private ITextComponent message;
    private String id;
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

    public boolean isValid(){
        return !getId().equals(INVALID_OBJECT);
    }

    public void compileMessage(){
        this.message = DialogueManager.parseDialogueMessage(rawMessage, dialogueTree);
    }

    public IFormattableTextComponent getMessage(LivingEntity source, ServerPlayerEntity target) {
        StringTextComponent name = new StringTextComponent(String.format("<%s> ",
                source.getDisplayName().getString()));
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
            name.getSiblings().add(comp);
        }
        return name;
    }

    public <D> void deserialize(Dynamic<D> dynamic) {

        this.rawMessage = dynamic.get("message").asString("");
        this.id = dynamic.get("id").asString("invalid");
    }


    public <D> D serialize(DynamicOps<D> ops) {
        return ops.createMap(ImmutableMap.of(
                ops.createString("message"), ops.createString(rawMessage),
                ops.createString("id"), ops.createString(id)
        ));
    }
}
