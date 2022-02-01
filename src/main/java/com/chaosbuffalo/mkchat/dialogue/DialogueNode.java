package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DialogueNode extends DialogueObject {
    private final List<DialogueEffect> effects;

    public DialogueNode(String nodeId, String rawMessage) {
        super(nodeId, rawMessage);
        this.effects = new ArrayList<>();
    }

    public DialogueNode(String nodeId) {
        this(nodeId, EMPTY_MSG);
    }

    public DialogueNode copy() {
        DialogueNode newNode = new DialogueNode(getId());
        INBT nbt = serialize(NBTDynamicOps.INSTANCE);
        newNode.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, nbt));
        return newNode;
    }

    public List<DialogueEffect> getEffects() {
        return effects;
    }

    public void addEffect(DialogueEffect effect) {
        this.effects.add(effect);
    }

    public IFormattableTextComponent getSpeakerMessage(LivingEntity speaker, ServerPlayerEntity player) {
        // Generate a string that looks like: "<speaker_name> {message}"
        IFormattableTextComponent msg = new StringTextComponent("<")
                .appendSibling(speaker.getDisplayName())
                .appendString("> ");

        DialogueContext context = new DialogueContext(speaker, player, this);
        getMessage().getSiblings().stream().map(comp -> {
            if (comp instanceof ContextAwareTextComponent) {
                return ((ContextAwareTextComponent) comp).getContextFormattedTextComponent(context);
            } else {
                return comp.deepCopy();
            }
        }).forEach(msg::appendSibling);
        return msg;
    }

    public void sendMessage(ServerPlayerEntity player, LivingEntity source) {
        sendMessage(player, source, getSpeakerMessage(source, player));
    }

    public void sendMessageWithSibling(ServerPlayerEntity player, LivingEntity source,
                                       DialoguePrompt withAdditional) {
        IFormattableTextComponent message = getSpeakerMessage(source, player)
                .appendString(" ")
                .appendSibling(withAdditional.getPromptLink());

        sendMessage(player, source, message);
    }

    private void sendMessage(ServerPlayerEntity player, LivingEntity source, ITextComponent message) {
        if (player.getServer() != null) {
            DialogueUtils.sendMessageToAllAround(player.getServer(), source, message);
            for (DialogueEffect effect : effects) {
                effect.applyEffect(player, source, this);
            }
        }
    }

    public static <D> DataResult<DialogueNode> fromDynamic(Dynamic<D> dynamic) {
        Optional<String> name = decodeKey(dynamic);
        if (!name.isPresent()) {
            return DataResult.error(String.format("Failed to decode dialogue node id: %s", dynamic));
        }

        DialogueNode prompt = new DialogueNode(name.get());
        prompt.deserialize(dynamic);
        if (prompt.isValid()) {
            return DataResult.success(prompt);
        }
        return DataResult.error(String.format("Unable to decode dialogue node: %s", name.get()));
    }

    public static <D> DialogueNode fromDynamicField(OptionalDynamic<D> dynamic) {
        return dynamic.flatMap(DialogueNode::fromDynamic)
                .resultOrPartial(DialogueUtils::throwParseException)
                .orElseThrow(IllegalStateException::new);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        if (effects.size() > 0) {
            builder.put(ops.createString("effects"), ops.createList(effects.stream().map(x -> x.serialize(ops))));
        }
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        effects.clear();
        dynamic.get("effects")
                .asList(DialogueEffect::fromDynamic)
                .forEach(dr -> dr.resultOrPartial(DialogueUtils::throwParseException).ifPresent(effects::add));
    }
}
