package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.MKChat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class DialogueTree {
    private final ResourceLocation dialogueName;
    private final Map<String, DialogueNode> nodes;
    private final Map<String, DialoguePrompt> prompts;
    private DialoguePrompt hailPrompt;

    public DialogueTree(ResourceLocation dialogueName) {
        this.dialogueName = dialogueName;
        this.nodes = new HashMap<>();
        this.prompts = new HashMap<>();
        hailPrompt = null;
    }

    public void addNode(DialogueNode node) {
        nodes.put(node.getId(), node);
        node.setDialogueTree(this);
    }

    public void setHailPrompt(DialoguePrompt hailPrompt) {
        this.hailPrompt = hailPrompt;
    }

    @Nullable
    public DialogueNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public ResourceLocation getDialogueName() {
        return dialogueName;
    }

    @Nullable
    public DialoguePrompt getPrompt(String name) {
        return prompts.get(name);
    }

    @Nullable
    public DialoguePrompt getHailPrompt() {
        return hailPrompt;
    }

    public void addPrompt(DialoguePrompt prompt) {
        prompts.put(prompt.getId(), prompt);
        prompt.setDialogueTree(this);
    }

    public boolean handlePlayerMessage(ServerPlayerEntity player, String message, LivingEntity speaker) {
        for (DialoguePrompt prompt : prompts.values()) {
            if (prompt.willTriggerFrom(message)) {
                if (prompt.handlePrompt(player, speaker, this, null)) {
                    return true;
                }
            }
        }
        return false;
    }

    private <T extends DialogueObject, D> D serializeList(DynamicOps<D> ops, Map<String, T> nodes) {
        ImmutableList.Builder<D> builder = ImmutableList.builder();
        nodes.forEach((key, value) -> builder.add(value.serialize(ops)));
        return ops.createList(builder.build().stream());
    }

    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("nodes"), serializeList(ops, nodes));
        builder.put(ops.createString("prompts"), serializeList(ops, prompts));

        if (getHailPrompt() != null) {
            builder.put(ops.createString("hailPrompt"), ops.createString(getHailPrompt().getId()));
        }
        return ops.createMap(builder.build());
    }

    public static <D> DialogueTree deserializeTreeFromDynamic(ResourceLocation name, Dynamic<D> dynamic) {
        DialogueTree tree = new DialogueTree(name);
        tree.deserialize(dynamic);
        return tree;
    }

    public <D> void deserialize(Dynamic<D> dynamic) {
        nodes.clear();
        dynamic.get("nodes").asList(DialogueNode::fromDynamic)
                .forEach(dr -> dr.resultOrPartial(MKChat.LOGGER::error).map(node -> {
                    addNode(node);
                    return node;
                }).orElseThrow(DialogueDataParsingException::new));

        prompts.clear();
        dynamic.get("prompts").asList(DialoguePrompt::fromDynamic)
                .forEach(dr -> dr.resultOrPartial(MKChat.LOGGER::error).map(prompt -> {
                    addPrompt(prompt);
                    return prompt;
                }).orElseThrow(DialogueDataParsingException::new));

        dynamic.get("hailPrompt").asString()
                .resultOrPartial(MKChat.LOGGER::error)
                .ifPresent(s -> setHailPrompt(getPrompt(s)));
    }
}
