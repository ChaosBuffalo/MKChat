package com.chaosbuffalo.mkchat.dialogue.conditions;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

public abstract class DialogueCondition implements IDynamicMapTypedSerializer {
    public static final ResourceLocation INVALID_CONDITION_TYPE = new ResourceLocation(MKChat.MODID, "dialogue_condition.invalid");
    public static final String TYPE_ENTRY_NAME = "dialogueConditionType";
    private final ResourceLocation conditionType;
    private boolean invert;

    public DialogueCondition(ResourceLocation conditionType){
        this.conditionType = conditionType;
        invert = false;
    }

    public abstract boolean meetsCondition(ServerPlayerEntity player, LivingEntity source);

    public boolean checkCondition(ServerPlayerEntity player, LivingEntity source){
        boolean condition = meetsCondition(player, source);
        MKChat.LOGGER.debug("Player {} meets condition {} {}", player, getTypeName(), invert != condition);
        return invert != condition;
    }

    public DialogueCondition setInvert(boolean invert) {
        this.invert = invert;
        return this;
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        builder.put(ops.createString("invert"), ops.createBoolean(invert));
    }

    @Override
    public ResourceLocation getTypeName() {
        return conditionType;
    }

    @Override
    public String getTypeEntryName() {
        return TYPE_ENTRY_NAME;
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        invert = dynamic.get("invert").asBoolean(false);
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic){
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_ENTRY_NAME).orElse(INVALID_CONDITION_TYPE);
    }

}
