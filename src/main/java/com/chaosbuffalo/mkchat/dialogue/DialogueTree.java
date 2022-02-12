package com.chaosbuffalo.mkchat.dialogue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
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
        node.setDialogueTree(this);
        nodes.put(node.getId(), node);
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

    public DialogueTree copy(){
        DialogueTree newTree = new DialogueTree(dialogueName);
        INBT nbt = serialize(NBTDynamicOps.INSTANCE);
        newTree.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, nbt));
        return newTree;
    }

    public void addPrompt(DialoguePrompt prompt) {
        prompt.setDialogueTree(this);
        prompt.getRequiredNodes().forEach(nodeId -> {
            DialogueNode node = getNode(nodeId);
            if (node == null) {
                throw new DialogueElementMissingException("Dialogue node '%s' needed by prompt '%s' was missing from tree '%s'", nodeId, prompt.getId(), getDialogueName());
            }
        });
        prompts.put(prompt.getId(), prompt);
    }

    public Map<String, DialogueNode> getNodes() {
        return nodes;
    }

    public Map<String, DialoguePrompt> getPrompts() {
        return prompts;
    }

    @Nullable
    public DialoguePrompt getHailPrompt() {
        return hailPrompt;
    }

    public void setHailPrompt(DialoguePrompt hailPrompt) {
        this.hailPrompt = hailPrompt;
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
                .forEach(dr -> dr.resultOrPartial(DialogueUtils::throwParseException).ifPresent(this::addNode));

        prompts.clear();
        dynamic.get("prompts").asList(DialoguePrompt::fromDynamic)
                .forEach(dr -> dr.resultOrPartial(DialogueUtils::throwParseException).ifPresent(this::addPrompt));

        dynamic.get("hailPrompt").asString()
                .resultOrPartial(DialogueUtils::throwParseException)
                .ifPresent(s -> {
                    DialoguePrompt prompt = getPrompt(s);
                    if (prompt != null) {
                        setHailPrompt(prompt);
                    } else {
                        throw new DialogueElementMissingException("Hail prompt '%s' not found in tree '%s'", s, getDialogueName());
                    }
                });
    }

    protected void internalMerge(DialogueTree other){
        boolean shouldMergeHailPrompt = true;
        if (getHailPrompt() != null){
            if (other.getHailPrompt() != null){
                getHailPrompt().merge(other.getHailPrompt().copy());
                shouldMergeHailPrompt = false;
            }
        } else {
            if (other.getHailPrompt() != null){
                setHailPrompt(other.getHailPrompt().copy());
            }
        }
        for (DialogueNode node : other.getNodes().values()){
            addNode(node.copy());
        }
        for (DialoguePrompt prompt : other.getPrompts().values()){
            if (!prompt.equals(other.getHailPrompt()) || shouldMergeHailPrompt){
                addPrompt(prompt.copy());
            }
        }
    }

    public DialogueTree merge(DialogueTree other){
        DialogueTree newTree = copy();
        newTree.internalMerge(other);
        return newTree;
    }
}
