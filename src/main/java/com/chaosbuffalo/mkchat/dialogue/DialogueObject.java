package com.chaosbuffalo.mkchat.dialogue;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Lazy;

import java.util.Optional;
import java.util.function.Supplier;

public class DialogueObject {
    public static final String INVALID_OBJECT = "invalid";
    public static final String EMPTY_MSG = "";
    private String id;
    private String rawMessage;
    private DialogueTree dialogueTree;
    private Supplier<ITextComponent> compiledMessage;

    public DialogueObject(String id, String rawMessage) {
        this.id = id;
        this.rawMessage = rawMessage;
        buildMessageSupplier();
    }

    public String getId() {
        return id;
    }

    private String getRawMessage() {
        return rawMessage;
    }

    public void setDialogueTree(DialogueTree dialogueTree) {
        this.dialogueTree = dialogueTree;
    }

    public DialogueTree getDialogueTree() {
        return dialogueTree;
    }

    public ITextComponent getMessage() {
        return compiledMessage.get();
    }

    public boolean isValid() {
        return !getId().equals(INVALID_OBJECT);
    }

    private void buildMessageSupplier() {
        compiledMessage = Lazy.of(() -> {
            if (getDialogueTree() == null) {
                throw new DialogueElementMissingException(
                        "Dialogue object '%s' was attempted to be compiled without a tree! Raw Message '%s'",
                        getId(), getRawMessage());
            }
            return DialogueManager.parseDialogueMessage(getRawMessage(), getDialogueTree());
        });
    }

    protected static <D> Optional<String> decodeKey(Dynamic<D> dynamic) {
        return dynamic.get("id").asString().resultOrPartial(DialogueUtils::throwParseException);
    }

    public final <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("id"), ops.createString(id));
        builder.put(ops.createString("message"), ops.createString(rawMessage));
        writeAdditionalData(ops, builder);
        return ops.createMap(builder.build());
    }

    public final <D> void deserialize(Dynamic<D> dynamic) {
        id = decodeKey(dynamic)
                .orElseThrow(IllegalStateException::new);
        rawMessage = dynamic.get("message").asString()
                .resultOrPartial(DialogueUtils::throwParseException)
                .orElseThrow(IllegalStateException::new);
        readAdditionalData(dynamic);
        buildMessageSupplier();
    }

    public <D> void readAdditionalData(Dynamic<D> dynamic) {
    }

    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
    }
}
