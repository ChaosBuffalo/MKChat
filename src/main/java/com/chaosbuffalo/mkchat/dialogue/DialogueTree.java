package com.chaosbuffalo.mkchat.dialogue;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DialogueTree {
    private final ResourceLocation dialogueName;
    private final Map<String, DialogueNode> nodes;
    private final Map<String, DialoguePrompt> prompts;
    private DialoguePrompt hailPrompt;

    public DialogueTree(ResourceLocation dialogueName){
        this.dialogueName = dialogueName;
        this.nodes = new HashMap<>();
        this.prompts = new HashMap<>();
        hailPrompt = null;
    }

    public void addNode(DialogueNode node){
        nodes.put(node.getId(), node);
        node.setDialogueTree(this);
    }

    public void bake(){
        for (DialoguePrompt prompt : prompts.values()){
            prompt.compileMessage();
        }
        for (DialogueNode node : nodes.values()){
            node.compileMessage();
        }
    }

    public void setHailPrompt(DialoguePrompt hailPrompt) {
        this.hailPrompt = hailPrompt;
    }

    @Nullable
    public DialogueNode getNode(String nodeId){
        return nodes.get(nodeId);
    }

    public ResourceLocation getDialogueName() {
        return dialogueName;
    }

    @Nullable
    public DialoguePrompt getPrompt(String name){
        return prompts.get(name);
    }

    @Nullable
    public DialoguePrompt getHailPrompt() {
        return hailPrompt;
    }

    public void addPrompt(DialoguePrompt prompt){
        prompts.put(prompt.getId(), prompt);
        prompt.setDialogueTree(this);
    }


    public void handlePlayerMessage(ServerPlayerEntity player, String message, LivingEntity speaker){
        for (DialoguePrompt prompt : prompts.values()){
            if (prompt.doesMatchInput(message)){
                prompt.handlePrompt(player, speaker, this);
            }
        }
    }

    public <D> D serialize(DynamicOps<D> ops){
        D ret = ops.createMap(ImmutableMap.of(
                ops.createString("nodes"),
                ops.createMap(nodes.entrySet().stream().map(entry -> Pair.of(
                        ops.createString(entry.getKey()),
                        entry.getValue().serialize(ops)))
                        .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
                ),
                ops.createString("prompts"),
                ops.createMap(prompts.entrySet().stream().map(entry -> Pair.of(
                        ops.createString(entry.getKey()),
                        entry.getValue().serialize(ops)))
                        .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
                )
        ));
        if (getHailPrompt() != null){
            ret = ops.mergeToMap(ret, ops.createString("hailPrompt"), ops.createString(getHailPrompt().getId())).result()
                    .orElse(ret);
        }
        return ret;
    }

    public static <D> DialogueTree deserializeTreeFromDynamic(ResourceLocation name, Dynamic<D> dynamic){
        DialogueTree tree = new DialogueTree(name);
        tree.deserialize(dynamic);
        return tree;
    }

    public <D> void deserialize(Dynamic<D> dynamic) {
        Map<String, Dynamic<D>> nodesDeserialized = dynamic.get("nodes").asMap(keyD -> keyD.asString(DialogueNode.INVALID_OBJECT),
                Function.identity());
        nodes.clear();
        for (Map.Entry<String, Dynamic<D>> nodeEntry : nodesDeserialized.entrySet()){
            DialogueNode node = new DialogueNode(nodeEntry.getKey());
            node.deserialize(nodeEntry.getValue());
            if (node.isValid()){
                addNode(node);
            }
        }
        Map<String, Dynamic<D>> promptsDeserialized = dynamic.get("prompts").asMap(
                keyD -> keyD.asString(DialoguePrompt.INVALID_OBJECT),
                Function.identity());
        prompts.clear();
        for (Map.Entry<String, Dynamic<D>> promptEntry : promptsDeserialized.entrySet()){
            DialoguePrompt prompt = new DialoguePrompt(promptEntry.getKey());
            prompt.deserialize(promptEntry.getValue());
            if (prompt.isValid()){
                addPrompt(prompt);
            }
        }
        dynamic.get("hailPrompt").asString().result().ifPresent(s -> setHailPrompt(getPrompt(s)));
    }
}
