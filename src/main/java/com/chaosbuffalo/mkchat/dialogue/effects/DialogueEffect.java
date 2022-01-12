package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.mojang.serialization.Dynamic;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

public abstract class DialogueEffect implements IDynamicMapTypedSerializer {
    public final static ResourceLocation INVALID_EFFECT_TYPE = new ResourceLocation(MKChat.MODID, "dialogue_effect.invalid");
    private static final String TYPE_ENTRY_NAME = "dialogueEffectType";
    private final ResourceLocation effectType;

    public DialogueEffect(ResourceLocation effectType) {
        this.effectType = effectType;
    }

    public abstract void applyEffect(ServerPlayerEntity player, LivingEntity source, DialogueNode node);

    @Override
    public String getTypeEntryName() {
        return TYPE_ENTRY_NAME;
    }

    @Override
    public ResourceLocation getTypeName() {
        return effectType;
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic) {
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_ENTRY_NAME).orElse(INVALID_EFFECT_TYPE);
    }
}
