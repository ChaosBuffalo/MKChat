package com.chaosbuffalo.mkchat.entity;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.capabilities.INpcDialogue;
import com.chaosbuffalo.mkchat.init.ChatEntityTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class TestChatReceiverEntity extends PigEntity {

    public TestChatReceiverEntity(final EntityType<? extends TestChatReceiverEntity> entityType, World world) {
        super(entityType, world);
        setCustomName(new StringTextComponent("Talking Pig"));
        INpcDialogue.get(this).ifPresent(cap -> cap.setDialogueTree(new ResourceLocation(MKChat.MODID, "test")));
    }

    public TestChatReceiverEntity(World world) {
        this(ChatEntityTypes.TEST_CHAT.get(), world);
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(1, new LookRandomlyGoal(this));
    }

    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vector3d vec, Hand hand) {
        if (!player.world.isRemote()) {
            INpcDialogue.get(this).ifPresent(cap -> cap.hail((ServerPlayerEntity) player));

        }
        return ActionResultType.SUCCESS;
    }
}
