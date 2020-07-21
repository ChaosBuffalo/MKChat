package com.chaosbuffalo.mkchat.entity;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.ContextStringTextComponent;
import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.DialoguePrompt;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkchat.init.ChatEntityTypes;
import com.google.common.collect.Lists;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class TestChatReceiverEntity extends PigEntity implements IPlayerChatReceiver{
    private final DialogueComponent dialogueComponent;

    public TestChatReceiverEntity(final EntityType<? extends TestChatReceiverEntity> entityType, World world) {
        super(entityType, world);
        this.dialogueComponent = createDialogueComponent();
        setCustomName(new StringTextComponent("Talking Pig"));
    }

    protected DialogueComponent createDialogueComponent(){
        DialogueTree dialogueTree = new DialogueTree(new ResourceLocation("mkchat", "test_receiver"));
        ITextComponent msg = new ContextStringTextComponent("Hello %s, I am %s. Do you ", (context ->
                Lists.newArrayList(context.getPlayer().getName().getFormattedText(),
                        context.getSpeaker().getName().getFormattedText())));
        DialoguePrompt testPrompt = new DialoguePrompt("need_xp", "need xp",
                "I need xp.");
        dialogueTree.addPrompt(testPrompt);
        msg.appendSibling(testPrompt.getPromptLink("need some xp"));
        msg.appendSibling(new StringTextComponent("?"));
        DialogueNode rootNode = new DialogueNode("root", msg);
        dialogueTree.addNode(rootNode);
        dialogueTree.setStartNode(rootNode);
        DialogueNode responseNode = new DialogueNode("grant_level",
                new StringTextComponent("Here is 1 level."));
        responseNode.setCallback((player) -> player.addExperienceLevel(1));
        testPrompt.setResultNode(responseNode);
        dialogueTree.addNode(responseNode);
        return new DialogueComponent(this, dialogueTree);
    }

    public TestChatReceiverEntity(World world){
        this(ChatEntityTypes.TEST_CHAT.get(), world);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(1, new LookRandomlyGoal(this));
    }

    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vec3d vec, Hand hand) {
        if (!player.world.isRemote()){
            dialogueComponent.startDialogue((ServerPlayerEntity) player);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void receiveMessage(ServerPlayerEntity player, String message) {
        MKChat.LOGGER.info("Received chat message from {}, {}", player, message);
        dialogueComponent.receiveMessageFromPlayer(player, message);
    }
}
