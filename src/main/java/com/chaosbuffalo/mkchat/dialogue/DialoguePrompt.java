package com.chaosbuffalo.mkchat.dialogue;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DialoguePrompt extends DialogueObject {
    private String promptPhrase;
    private String defaultPromptText;
    public static String EMPTY_PROMPT_PHRASE = "";
    public static String EMPTY_PROMPT_TEXT = "";
    private final List<DialogueResponse> responses;

    public DialoguePrompt(String promptId, String promptPhrase, String defaultPromptText, String text){
        super(promptId, text);
        this.promptPhrase = promptPhrase;
        this.defaultPromptText = defaultPromptText;
        this.responses = new ArrayList<>();
    }

    public DialoguePrompt(String promptId){
        this(promptId, EMPTY_PROMPT_PHRASE, EMPTY_PROMPT_TEXT, EMPTY_MSG);
    }

    public DialoguePrompt(){
        this(INVALID_OBJECT, EMPTY_PROMPT_PHRASE, EMPTY_PROMPT_TEXT, EMPTY_MSG);
    }

    public DialoguePrompt addResponse(DialogueResponse response){
        responses.add(response);
        return this;
    }

    public List<DialogueResponse> getResponses() {
        return responses;
    }

    public DialoguePrompt copy(){
        DialoguePrompt newPrompt = new DialoguePrompt();
        INBT nbt = serialize(NBTDynamicOps.INSTANCE);
        newPrompt.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, nbt));
        return newPrompt;
    }

    public String getPromptPhrase() {
        return promptPhrase;
    }

    public String getDefaultPromptText() {
        return defaultPromptText;
    }

    public boolean doesMatchInput(String input){
        return !promptPhrase.equals(EMPTY_PROMPT_PHRASE) && input.contains(promptPhrase);
    }

    public String getPromptEmbed(){
        return String.format("{prompt:%s}", getId());
    }

    public boolean handlePrompt(ServerPlayerEntity player, LivingEntity source, DialogueTree tree, @Nullable DialoguePrompt withAdditional){
        for (DialogueResponse response : responses){
            if (response.doesMatchConditions(player, source)){
                DialogueNode responseNode = tree.getNode(response.getResponseNodeId());
                if (responseNode != null){
                    if (withAdditional != null){
                        responseNode.sendMessageWithSibling(player, source, withAdditional);
                    } else {
                        responseNode.sendMessage(player, source);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean willHandle(ServerPlayerEntity player, LivingEntity source){
        for (DialogueResponse response : responses) {
            if (response.doesMatchConditions(player, source)) {
                return true;
            }
        }
        return false;
    }

    public ITextComponent getPromptLink() {
        StringTextComponent textComponent = new StringTextComponent(String.format("[%s]",
                getMessage().getString()));
        textComponent.mergeStyle(TextFormatting.AQUA);
        textComponent.setStyle(textComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                getDefaultPromptText())));
        return textComponent;
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        D ret = super.serialize(ops);
        ret = ops.mergeToMap(ret, ImmutableMap.of(
                ops.createString("promptPhrase"), ops.createString(promptPhrase),
                ops.createString("promptText"), ops.createString(defaultPromptText)
        )).result().orElse(ret);
        if (responses.size() > 0){
            ret = ops.mergeToMap(
                    ret,
                    ops.createString("responses"),
                    ops.createList(responses.stream().map(x -> x.serialize(ops)))
            ).result().orElse(ret);
        }
        return ret;
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        super.deserialize(dynamic);
        this.promptPhrase = dynamic.get("promptPhrase").asString(EMPTY_PROMPT_PHRASE);
        this.defaultPromptText = dynamic.get("promptText").asString(EMPTY_PROMPT_TEXT);
        List<DialogueResponse> deserializedResponses = dynamic.get("responses").asList(d -> {
                    DialogueResponse resp = new DialogueResponse();
                    resp.deserialize(d);
                    return resp;
                });
        responses.clear();
        for (DialogueResponse resp : deserializedResponses){
            if (resp.isValid()){
                responses.add(resp);
            }
        }
    }
}
