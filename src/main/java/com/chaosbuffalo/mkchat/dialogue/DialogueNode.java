package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DialogueNode {
    private final String nodeId;
    private final ITextComponent message;
    private Consumer<ServerPlayerEntity> callback;

    public DialogueNode(String nodeId, ITextComponent message){
        this.nodeId = nodeId;
        this.message = message;
        this.callback = null;
    }

    public void sendMessage(ServerPlayerEntity player, LivingEntity source){
        if (player.getServer() != null){
            DialogueUtils.sendMessageToAllAround(player.getServer(), source, getMessage(source, player));
            if (getCallback() != null){
                callback.accept(player);
            }
        }
    }

    public void setCallback(Consumer<ServerPlayerEntity> callback) {
        this.callback = callback;
    }

    @Nullable
    public Consumer<ServerPlayerEntity> getCallback() {
        return callback;
    }

    public String getNodeId() {
        return nodeId;
    }


    public ITextComponent getMessage(LivingEntity source, ServerPlayerEntity target) {
        ITextComponent name = new StringTextComponent(String.format("<%s> ",
                source.getDisplayName().getFormattedText()));
        DialogueContext context = new DialogueContext(source, target, this);
        Stream<ITextComponent> finalMsg = message.stream().map((comp -> {
            if (comp instanceof ContextAwareTextComponent){
                return ((ContextAwareTextComponent) comp).getContextFormattedTextComponent(context);
            } else {
               return comp;
            }
        }));
        for (ITextComponent comp : finalMsg.collect(Collectors.toList())){
            name.appendSibling(comp);
        }
        return name;
    }
}
