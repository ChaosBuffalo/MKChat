package com.chaosbuffalo.mkchat.dialogue.conditions;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.capabilities.ChatCapabilities;
import com.chaosbuffalo.mkchat.dialogue.effects.AddFlag;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

public class HasBoolFlagCondition extends DialogueCondition {
    public static final ResourceLocation conditionTypeName = new ResourceLocation(MKChat.MODID, "dialogue_condition.has_bool_flag");
    private ResourceLocation flagName;

    public HasBoolFlagCondition(ResourceLocation flagName){
        super(conditionTypeName);
        this.flagName = flagName;
    }

    public HasBoolFlagCondition(){
        this(AddFlag.INVALID_FLAG);
    }

    @Override
    public boolean meetsCondition(ServerPlayerEntity player, LivingEntity source) {
        if (flagName.equals(AddFlag.INVALID_FLAG)){
            return false;
        }
        return player.getCapability(ChatCapabilities.PLAYER_DIALOGUE_CAPABILITY).map(cap ->
                cap.getNPCEntry(source.getUniqueID()).getBoolFlag(flagName)).orElse(false);
    }


    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        this.flagName = new ResourceLocation(dynamic.get("flagName").asString().result().orElse(AddFlag.INVALID_FLAG.toString()));
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("flagName"), ops.createString(flagName.toString()));
    }
}
