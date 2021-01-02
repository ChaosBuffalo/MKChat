package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

public class AddLevelEffect extends DialogueEffect {
    private int levelAmount;
    public static ResourceLocation effectTypeName = new ResourceLocation(MKChat.MODID, "dialogue_effect.add_level");

    public AddLevelEffect(int levelAmount){
        super(effectTypeName);
        this.levelAmount = levelAmount;
    }

    public AddLevelEffect(){
        this(0);
    }

    @Override
    public void applyEffect(ServerPlayerEntity player, LivingEntity source, DialogueNode node) {
        player.addExperienceLevel(levelAmount);
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        this.levelAmount = dynamic.get("amount").asInt(0);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        D ret = super.serialize(ops);
        return ops.mergeToMap(ret, ImmutableMap.of(
           ops.createString("amount"), ops.createInt(levelAmount)
        )).result().orElse(ret);
    }
}
