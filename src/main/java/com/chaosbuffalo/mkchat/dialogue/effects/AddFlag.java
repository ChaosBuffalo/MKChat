package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.capabilities.ChatCapabilities;
import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

public class AddFlag extends DialogueEffect{
    public static ResourceLocation effectTypeName = new ResourceLocation(MKChat.MODID, "dialogue_effect.add_flag");
    public static ResourceLocation INVALID_FLAG = new ResourceLocation(MKChat.MODID, "invalid_flag");
    private ResourceLocation flagName;

    public AddFlag(ResourceLocation flagName){
        super(effectTypeName);
        this.flagName = flagName;
    }

    public AddFlag(){
        this(INVALID_FLAG);
    }

    @Override
    public void applyEffect(ServerPlayerEntity player, LivingEntity source, DialogueNode node) {
        if (flagName.equals(INVALID_FLAG)){
            return;
        }
        player.getCapability(ChatCapabilities.PLAYER_DIALOGUE_CAPABILITY).ifPresent(cap ->
                cap.getNPCEntry(source.getUniqueID()).putBoolFlag(flagName, true));
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        D ret = super.serialize(ops);
        return ops.mergeToMap(ret, ImmutableMap.of(
                ops.createString("flagName"), ops.createString(flagName.toString())
        )).result().orElse(ret);
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        this.flagName = new ResourceLocation(dynamic.get("flagName").asString().result().orElse(INVALID_FLAG.toString()));
    }
}
