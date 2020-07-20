package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;

public class DialogueNode {
    private final UUID nodeId;
    private final ITextComponent message;
    private Consumer<ServerPlayerEntity> callback;

    public DialogueNode(UUID nodeId, ITextComponent message){
        this.nodeId = nodeId;
        this.message = message;
        this.callback = null;
    }

    public void sendMessage(ServerPlayerEntity player, LivingEntity source){
        if (player.getServer() != null){
            DialogueUtils.sendMessageToAllAround(player.getServer(), source, getMessage(source));
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

    public UUID getNodeId() {
        return nodeId;
    }

    public ITextComponent getMessage(LivingEntity source) {
        ITextComponent name = new StringTextComponent(String.format("<%s> ", source.getDisplayName().getFormattedText()));
        name.appendSibling(message);
        return name;
    }
}
